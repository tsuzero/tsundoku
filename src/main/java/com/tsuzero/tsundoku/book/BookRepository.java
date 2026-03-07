package com.tsuzero.tsundoku.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByGoodreadsId(String goodreadsId);

    @Query("""
            SELECT b FROM Book b
            LEFT JOIN FETCH b.availability
            ORDER BY b.title ASC
            """)
    List<Book> findAllWithAvailability();

    @Query("""
            SELECT b FROM Book b
            LEFT JOIN FETCH b.availability a
            WHERE a.available = :available
            ORDER BY b.title ASC
            """)
    List<Book> findByAvailabilityStatus(Boolean available);

    @Query("""
            SELECT b FROM Book b
            LEFT JOIN FETCH b.availability a
            WHERE a IS NULL OR a.checkedAt IS NULL
            ORDER BY b.title ASC
            """)
    List<Book> findWithUnknownAvailability();
}
