package com.tsuzero.tsundoku.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryAvailabilityRepository extends JpaRepository<LibraryAvailability, Long> {

    Optional<LibraryAvailability> findByBookId(Long bookId);

    @Query("SELECT la FROM LibraryAvailability la JOIN FETCH la.book WHERE la.id = :id")
    Optional<LibraryAvailability> findByIdWithBook(Long id);

    /**
     * Select books needing a Finna availability check, ordered by priority:
     * 1. Never checked (NULL checked_at)
     * 2. Available = true → re-check after 48h
     * 3. Available = false → re-check after 7 days
     * 4. Not found in Finna (finna_record_id IS NULL) → re-check after 30 days
     */
    @Query(value = """
            SELECT la.* FROM library_availability la
            WHERE la.checked_at IS NULL
               OR (la.available = true  AND la.checked_at < NOW() - INTERVAL '48 hours')
               OR (la.available = false AND la.checked_at < NOW() - INTERVAL '7 days')
               OR (la.finna_record_id IS NULL AND la.checked_at < NOW() - INTERVAL '30 days')
            ORDER BY la.checked_at ASC NULLS FIRST
            LIMIT :limit
            """, nativeQuery = true)
    List<LibraryAvailability> findBooksNeedingCheck(int limit);

    @Query(value = """
            SELECT la.* FROM library_availability la
            JOIN books b ON b.id = la.book_id
            WHERE la.checked_at IS NULL
            ORDER BY la.checked_at ASC NULLS FIRST
            LIMIT :limit
            """, nativeQuery = true)
    List<LibraryAvailability> findUnchecked(int limit);
}
