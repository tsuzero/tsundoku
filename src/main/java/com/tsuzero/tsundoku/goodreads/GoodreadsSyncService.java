package com.tsuzero.tsundoku.goodreads;

import com.tsuzero.tsundoku.book.BookService;
import com.tsuzero.tsundoku.config.FeaturesProperties;
import com.tsuzero.tsundoku.sync.SyncLog;
import com.tsuzero.tsundoku.sync.SyncLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class GoodreadsSyncService {

    private static final Logger log = LoggerFactory.getLogger(GoodreadsSyncService.class);

    private final GoodreadsRssClient rssClient;
    private final BookService bookService;
    private final SyncLogRepository syncLogRepository;
    private final FeaturesProperties features;

    public GoodreadsSyncService(GoodreadsRssClient rssClient,
                                BookService bookService,
                                SyncLogRepository syncLogRepository,
                                FeaturesProperties features) {
        this.rssClient = rssClient;
        this.bookService = bookService;
        this.syncLogRepository = syncLogRepository;
        this.features = features;
    }

    @Scheduled(cron = "${sync.goodreads.cron}")
    public void scheduledSync() {
        if (!features.isGoodreadsSyncEnabled()) {
            log.debug("Goodreads sync disabled by feature flag");
            return;
        }
        sync();
    }

    public SyncLog sync() {
        SyncLog syncLog = new SyncLog();
        syncLog.setSyncType("GOODREADS");
        syncLog.setStartedAt(OffsetDateTime.now());
        syncLogRepository.save(syncLog);

        int processed = 0;
        try {
            List<BookService.BookData> books = rssClient.fetchToReadShelf();
            for (BookService.BookData data : books) {
                try {
                    bookService.upsert(data);
                    processed++;
                } catch (Exception e) {
                    log.warn("Failed to upsert book: {}", data.title(), e);
                }
            }
            syncLog.setStatus("SUCCESS");
            log.info("Goodreads sync completed: {} books processed", processed);
        } catch (Exception e) {
            log.error("Goodreads sync failed", e);
            syncLog.setStatus("FAILED");
            syncLog.setErrorMessage(e.getMessage());
        }

        syncLog.setFinishedAt(OffsetDateTime.now());
        syncLog.setBooksProcessed(processed);
        return syncLogRepository.save(syncLog);
    }
}
