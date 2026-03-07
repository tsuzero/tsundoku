CREATE TABLE books (
    id                   BIGSERIAL PRIMARY KEY,
    goodreads_id         VARCHAR(50)  UNIQUE NOT NULL,
    isbn                 VARCHAR(13),
    isbn13               VARCHAR(13),
    title                VARCHAR(500) NOT NULL,
    author_name          VARCHAR(300) NOT NULL,
    cover_url            TEXT,
    description          TEXT,
    average_rating       NUMERIC(3,2),
    year_published       INTEGER,
    date_added_to_shelf  TIMESTAMPTZ,
    synced_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_books_isbn   ON books(isbn);
CREATE INDEX idx_books_isbn13 ON books(isbn13);
