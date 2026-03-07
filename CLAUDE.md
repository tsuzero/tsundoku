# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Full Dev Stack
```bash
scripts/start-local.sh   # Starts Docker Postgres + backend (8080)
```

### Backend
```bash
scripts/run-tests.sh                                                    # Run all tests (Testcontainers, no external DB)
./mvnw -f backend/pom.xml spring-boot:run                              # Run backend only
./mvnw -f backend/pom.xml test -Dtest=BookServiceTest                  # Run single test class
```

### Database
```bash
scripts/db-reset.sh   # Drop & recreate DB; next backend start re-applies Flyway migrations
```

## Architecture

### Data Flow
Goodreads RSS → `GoodreadsRssClient` (JDOM2 SAXBuilder) → `GoodreadsSyncService` (@Scheduled 6 AM daily) → `books` table

`books` → `FinnaSyncJob` (@Scheduled every 5 min, batch 20) → `FinnaClient` (WebClient) → `library_availability` table

Browser → Spring Boot (Thymeleaf + HTMX) → REST API (same process) → Database

### Backend Package Layout
- `fi.tsundoku.book` — Book + LibraryAvailability entities, BookDto, BookService (upsert/filter/sort), BookController
- `fi.tsundoku.goodreads` — GoodreadsRssClient (custom RSS field extraction), GoodreadsSyncService
- `fi.tsundoku.finna` — FinnaClient (WebClient), FinnaSyncJob (TTL-based batched checking)
- `fi.tsundoku.sync` — SyncLog entity, SyncController (status/trigger endpoints)
- `fi.tsundoku.config` — WebClientConfig (finnaWebClient bean)
- `fi.tsundoku.web` — BookWebController (@Controller), BookViewModel (star string helper)

### Web UI (Thymeleaf + HTMX)
- Templates in `backend/src/main/resources/templates/`
  - `fragments/ui.html` — named fragments: header, footer, bookList, bookCard, availabilityBadge, syncStatus, availabilitySection
  - `books/list.html` — full books list page (inserts bookList + syncStatus fragments)
  - `books/detail.html` — full book detail page (inserts availabilitySection fragment)
- Plain CSS served from `backend/src/main/resources/static/css/style.css` — no build step
- HTMX served from webjars (`/webjars/htmx.org/dist/htmx.min.js`)
- Filter/sort form uses `hx-get="/books"` with `hx-push-url="true"` — partial swap of `#book-list`
- Availability refresh: `hx-post="/books/{id}/availability/refresh"` → swaps `#availability-section`
- Goodreads sync button: `hx-post="/sync/goodreads"` → swaps `#sync-status`

### Finna Search Logic
- ISBN search first, falls back to `title:"{title}" AND author:{surname}`
- Strips series suffixes like ` (Wheel of Time, #1)` before searching
- Surname extraction skips suffixes (Jr, Sr, II, III, IV)
- Escapes Lucene phrase query characters
- Availability = book found in ≥1 `0/Helmet/` branch (not real-time checkout status)
- TTL: available→48h, unavailable→7d, not found in Finna→30d

### Goodreads RSS Parsing
- Custom fields extracted from `SyndEntry.getForeignMarkup()` (book_id, isbn, isbn13, author_name, etc.)
- Book ID fallback chain: `book_id` element → `<book id="">` attribute → image URL extraction
- "null" string → null for ISBN fields
- Date format: RFC 1123 → OffsetDateTime (UTC)

### Testing
- `@SpringBootTest` + `@Testcontainers` — spins up its own PostgreSQL 17 container
- No external database needed to run tests
- Tests live in `backend/src/test/java/`

### Schema Management
- Flyway migrations only (`ddl-auto: validate`) — never let Hibernate modify schema
- Migrations in `backend/src/main/resources/db/migration/` (V1, V2, V3)
- `open-in-view: false` — transactions must be explicit; lazy-loaded associations (Book → Availability) require active transaction

### Key Configuration (`backend/src/main/resources/application.yml`)
- `goodreads.user-id` — must be set for RSS sync to work
- `finna.helmet-building-filter: "0/Helmet/"` — filters results to Helmet libraries
- `sync.finna.fixed-delay-ms: 300000` and `sync.finna.batch-size: 20`
- `sync.goodreads.cron: "0 0 6 * * *"` — daily at 6 AM
