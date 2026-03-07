package com.tsuzero.tsundoku.book;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class BookServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("tsundoku_test")
            .withUsername("tsundoku")
            .withPassword("tsundoku");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    BookService bookService;

    @Test
    void upsertCreatesBook() {
        var data = new BookService.BookData(
                "gr-001",
                "1234567890",
                "1234567890123",
                "Test Book",
                "Test Author",
                "https://example.com/cover.jpg",
                "A test book description",
                new BigDecimal("4.50"),
                2024,
                OffsetDateTime.now()
        );

        bookService.upsert(data);

        List<BookDto> books = bookService.listBooks(null, null);
        assertThat(books).hasSize(1);
        assertThat(books.get(0).title()).isEqualTo("Test Book");
        assertThat(books.get(0).authorName()).isEqualTo("Test Author");
        assertThat(books.get(0).availability()).isNotNull();
    }

    @Test
    void upsertUpdatesExistingBook() {
        var data = new BookService.BookData(
                "gr-002",
                null, null,
                "Original Title",
                "Author Name",
                null, null, null, null, null
        );
        bookService.upsert(data);

        var updated = new BookService.BookData(
                "gr-002",
                null, null,
                "Updated Title",
                "Author Name",
                null, null, null, null, null
        );
        bookService.upsert(updated);

        List<BookDto> books = bookService.listBooks(null, null);
        long count = books.stream().filter(b -> b.goodreadsId().equals("gr-002")).count();
        assertThat(count).isEqualTo(1);

        BookDto book = books.stream().filter(b -> b.goodreadsId().equals("gr-002")).findFirst().orElseThrow();
        assertThat(book.title()).isEqualTo("Updated Title");
    }
}
