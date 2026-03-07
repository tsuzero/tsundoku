package com.tsuzero.tsundoku.book;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BookDto(
        Long id,
        String goodreadsId,
        String isbn,
        String isbn13,
        String title,
        String authorName,
        String coverUrl,
        String description,
        BigDecimal averageRating,
        Integer yearPublished,
        OffsetDateTime dateAddedToShelf,
        OffsetDateTime syncedAt,
        AvailabilityDto availability
) {
    public record AvailabilityDto(
            String finnaRecordId,
            Boolean available,
            Integer branchCount,
            String[] branches,
            OffsetDateTime checkedAt,
            String searchStrategy
    ) {}

    public static BookDto from(Book book) {
        AvailabilityDto availDto = null;
        if (book.getAvailability() != null) {
            LibraryAvailability a = book.getAvailability();
            availDto = new AvailabilityDto(
                    a.getFinnaRecordId(),
                    a.getAvailable(),
                    a.getBranchCount(),
                    a.getBranches(),
                    a.getCheckedAt(),
                    a.getSearchStrategy()
            );
        }
        return new BookDto(
                book.getId(),
                book.getGoodreadsId(),
                book.getIsbn(),
                book.getIsbn13(),
                book.getTitle(),
                book.getAuthorName(),
                book.getCoverUrl(),
                book.getDescription(),
                book.getAverageRating(),
                book.getYearPublished(),
                book.getDateAddedToShelf(),
                book.getSyncedAt(),
                availDto
        );
    }
}
