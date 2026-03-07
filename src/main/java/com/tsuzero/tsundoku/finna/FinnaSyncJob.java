package com.tsuzero.tsundoku.finna;

import com.tsuzero.tsundoku.book.Book;
import com.tsuzero.tsundoku.book.LibraryAvailability;
import com.tsuzero.tsundoku.book.LibraryAvailabilityRepository;
import com.tsuzero.tsundoku.config.FeaturesProperties;
import com.tsuzero.tsundoku.sync.SyncLog;
import com.tsuzero.tsundoku.sync.SyncLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class FinnaSyncJob {

    private static final Logger log = LoggerFactory.getLogger(FinnaSyncJob.class);
    private static final long POLITENESS_DELAY_MS = 500;

    private final FinnaClient finnaClient;
    private final LibraryAvailabilityRepository availabilityRepository;
    private final SyncLogRepository syncLogRepository;
    private final FeaturesProperties features;

    @Value("${sync.finna.batch-size:20}")
    private int batchSize;

    public FinnaSyncJob(FinnaClient finnaClient,
                        LibraryAvailabilityRepository availabilityRepository,
                        SyncLogRepository syncLogRepository,
                        FeaturesProperties features) {
        this.finnaClient = finnaClient;
        this.availabilityRepository = availabilityRepository;
        this.syncLogRepository = syncLogRepository;
        this.features = features;
    }

    @Scheduled(fixedDelayString = "${sync.finna.fixed-delay-ms:300000}")
    public void scheduledSync() {
        if (!features.isFinnaSyncEnabled()) {
            log.debug("Finna sync disabled by feature flag");
            return;
        }
        runBatch();
    }

    public SyncLog runBatch() {
        SyncLog syncLog = new SyncLog();
        syncLog.setSyncType("FINNA");
        syncLog.setStartedAt(OffsetDateTime.now());
        syncLogRepository.save(syncLog);

        int processed = 0;
        try {
            List<LibraryAvailability> batch = availabilityRepository.findBooksNeedingCheck(batchSize);
            log.info("Finna sync: processing {} books", batch.size());

            for (LibraryAvailability avail : batch) {
                try {
                    checkSingle(avail);
                    processed++;
                    Thread.sleep(POLITENESS_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.warn("Failed to check availability for avail id {}", avail.getId(), e);
                }
            }

            syncLog.setStatus(processed == batch.size() ? "SUCCESS" : "PARTIAL");
        } catch (Exception e) {
            log.error("Finna sync batch failed", e);
            syncLog.setStatus("FAILED");
            syncLog.setErrorMessage(e.getMessage());
        }

        syncLog.setFinishedAt(OffsetDateTime.now());
        syncLog.setBooksProcessed(processed);
        return syncLogRepository.save(syncLog);
    }

    @Transactional
    public void checkSingle(LibraryAvailability avail) {
        // Re-fetch within this transaction so the lazy Book proxy can be initialised
        avail = availabilityRepository.findByIdWithBook(avail.getId()).orElseThrow();
        Book book = avail.getBook();
        log.debug("Checking Finna availability for book: {}", book.getTitle());

        Optional<FinnaSearchResponse.FinnaRecord> record = Optional.empty();
        String strategy = "isbn";

        // Try ISBN first
        String isbn = book.getIsbn13() != null ? book.getIsbn13() : book.getIsbn();
        if (isbn != null && !isbn.isBlank()) {
            record = finnaClient.searchByIsbn(isbn);
        }

        // Fall back to title + author
        if (record.isEmpty()) {
            strategy = "title_author";
            record = finnaClient.searchByTitleAuthor(book.getTitle(), book.getAuthorName());
        }

        avail.setSearchStrategy(strategy);
        avail.setCheckedAt(OffsetDateTime.now());

        if (record.isPresent()) {
            FinnaSearchResponse.FinnaRecord r = record.get();
            avail.setFinnaRecordId(r.id());
            List<FinnaSearchResponse.Building> buildings = r.buildings() != null ? r.buildings() : List.of();

            // Keep human-readable branch names (translated), excluding the top-level "0/Helmet/" entry
            List<String> helmetBranches = buildings.stream()
                    .filter(b -> b.value() != null && b.value().startsWith("2/Helmet/"))
                    .map(b -> b.translated() != null ? b.translated() : b.value())
                    .toList();

            boolean foundInHelmet = buildings.stream()
                    .anyMatch(b -> "0/Helmet/".equals(b.value()));

            avail.setAvailable(foundInHelmet);
            avail.setBranchCount(helmetBranches.size());
            avail.setBranches(helmetBranches.toArray(String[]::new));
            log.debug("Book '{}' found in Finna: {} Helmet branches", book.getTitle(), helmetBranches.size());
        } else {
            avail.setFinnaRecordId(null);
            avail.setAvailable(false);
            avail.setBranchCount(0);
            avail.setBranches(new String[0]);
            log.debug("Book '{}' not found in Finna (Helmet)", book.getTitle());
        }

        availabilityRepository.save(avail);
    }
}
