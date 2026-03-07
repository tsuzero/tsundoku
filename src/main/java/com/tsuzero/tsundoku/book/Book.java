package com.tsuzero.tsundoku.book;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goodreads_id", unique = true, nullable = false, length = 50)
    private String goodreadsId;

    @Column(length = 13)
    private String isbn;

    @Column(length = 13)
    private String isbn13;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "author_name", nullable = false, length = 300)
    private String authorName;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "year_published")
    private Integer yearPublished;

    @Column(name = "date_added_to_shelf")
    private OffsetDateTime dateAddedToShelf;

    @Column(name = "synced_at", nullable = false)
    private OffsetDateTime syncedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LibraryAvailability availability;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        syncedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        syncedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGoodreadsId() { return goodreadsId; }
    public void setGoodreadsId(String goodreadsId) { this.goodreadsId = goodreadsId; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getIsbn13() { return isbn13; }
    public void setIsbn13(String isbn13) { this.isbn13 = isbn13; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

    public Integer getYearPublished() { return yearPublished; }
    public void setYearPublished(Integer yearPublished) { this.yearPublished = yearPublished; }

    public OffsetDateTime getDateAddedToShelf() { return dateAddedToShelf; }
    public void setDateAddedToShelf(OffsetDateTime dateAddedToShelf) { this.dateAddedToShelf = dateAddedToShelf; }

    public OffsetDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(OffsetDateTime syncedAt) { this.syncedAt = syncedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public LibraryAvailability getAvailability() { return availability; }
    public void setAvailability(LibraryAvailability availability) { this.availability = availability; }
}
