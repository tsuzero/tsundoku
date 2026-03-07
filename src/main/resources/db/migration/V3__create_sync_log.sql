CREATE TABLE sync_log (
    id               BIGSERIAL PRIMARY KEY,
    sync_type        VARCHAR(30) NOT NULL,   -- 'GOODREADS' | 'FINNA'
    started_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at      TIMESTAMPTZ,
    status           VARCHAR(20),            -- 'SUCCESS' | 'PARTIAL' | 'FAILED'
    books_processed  INTEGER DEFAULT 0,
    error_message    TEXT
);
