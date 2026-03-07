package com.tsuzero.tsundoku.book;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "library_availability")
public class LibraryAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    private Book book;

    @Column(name = "finna_record_id", length = 100)
    private String finnaRecordId;

    private Boolean available;

    @Column(name = "branch_count")
    private Integer branchCount;

    @Column(columnDefinition = "TEXT[]")
    private String[] branches;

    @Column(name = "checked_at")
    private OffsetDateTime checkedAt;

    @Column(name = "search_strategy", nullable = false, length = 20)
    private String searchStrategy = "isbn";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public String getFinnaRecordId() { return finnaRecordId; }
    public void setFinnaRecordId(String finnaRecordId) { this.finnaRecordId = finnaRecordId; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public Integer getBranchCount() { return branchCount; }
    public void setBranchCount(Integer branchCount) { this.branchCount = branchCount; }

    public String[] getBranches() { return branches; }
    public void setBranches(String[] branches) { this.branches = branches; }

    public OffsetDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(OffsetDateTime checkedAt) { this.checkedAt = checkedAt; }

    public String getSearchStrategy() { return searchStrategy; }
    public void setSearchStrategy(String searchStrategy) { this.searchStrategy = searchStrategy; }
}
