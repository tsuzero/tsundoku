package com.tsuzero.tsundoku.book;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final LibraryAvailabilityRepository availabilityRepository;

    public BookService(BookRepository bookRepository,
                       LibraryAvailabilityRepository availabilityRepository) {
        this.bookRepository = bookRepository;
        this.availabilityRepository = availabilityRepository;
    }

    public List<BookDto> listBooks(String available, String sort) {
        List<Book> books;

        if ("true".equals(available)) {
            books = bookRepository.findByAvailabilityStatus(true);
        } else if ("false".equals(available)) {
            books = bookRepository.findByAvailabilityStatus(false);
        } else if ("unknown".equals(available)) {
            books = bookRepository.findWithUnknownAvailability();
        } else {
            books = bookRepository.findAllWithAvailability();
        }

        Comparator<Book> comparator = switch (sort == null ? "title" : sort) {
            case "author" -> Comparator.comparing(Book::getAuthorName,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "rating" -> Comparator.comparing(Book::getAverageRating,
                    Comparator.nullsLast(Comparator.reverseOrder()));
            case "added" -> Comparator.comparing(Book::getDateAddedToShelf,
                    Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator.comparing(Book::getTitle,
                    Comparator.nullsLast(String::compareToIgnoreCase));
        };

        return books.stream()
                .sorted(comparator)
                .map(BookDto::from)
                .toList();
    }

    public Optional<BookDto> findById(Long id) {
        return bookRepository.findById(id).map(BookDto::from);
    }

    @Transactional
    public Book upsert(BookData data) {
        Optional<Book> existing = bookRepository.findByGoodreadsId(data.goodreadsId());
        Book book = existing.orElseGet(Book::new);

        book.setGoodreadsId(data.goodreadsId());
        book.setIsbn(data.isbn());
        book.setIsbn13(data.isbn13());
        book.setTitle(data.title());
        book.setAuthorName(data.authorName());
        book.setCoverUrl(data.coverUrl());
        book.setDescription(data.description());
        book.setAverageRating(data.averageRating());
        book.setYearPublished(data.yearPublished());
        book.setDateAddedToShelf(data.dateAddedToShelf());
        book.setSyncedAt(OffsetDateTime.now());

        book = bookRepository.save(book);

        // Create availability row if it doesn't exist
        if (book.getAvailability() == null) {
            LibraryAvailability avail = new LibraryAvailability();
            avail.setBook(book);
            avail.setSearchStrategy("isbn");
            availabilityRepository.save(avail);
        }

        return book;
    }

    public record BookData(
            String goodreadsId,
            String isbn,
            String isbn13,
            String title,
            String authorName,
            String coverUrl,
            String description,
            java.math.BigDecimal averageRating,
            Integer yearPublished,
            OffsetDateTime dateAddedToShelf
    ) {}
}
