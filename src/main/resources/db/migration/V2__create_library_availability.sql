CREATE TABLE library_availability (
    id              BIGSERIAL PRIMARY KEY,
    book_id         BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE UNIQUE,
    finna_record_id VARCHAR(100),
    available       BOOLEAN,
    branch_count    INTEGER,
    branches        TEXT[],
    checked_at      TIMESTAMPTZ,
    search_strategy VARCHAR(20) NOT NULL DEFAULT 'isbn'  -- 'isbn' | 'title_author'
);

CREATE INDEX idx_availability_checked ON library_availability(checked_at);
