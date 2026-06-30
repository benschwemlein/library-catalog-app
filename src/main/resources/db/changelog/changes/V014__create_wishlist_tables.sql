-- V014: Reading list / wishlist tables
-- Creates reading_list and reading_list_item tables
-- Members can curate personal book lists with sharing options

-- ============================================================
-- TABLE: reading_list
-- A named, ordered list of books curated by a member
-- ============================================================
CREATE TABLE reading_list (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT       NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    visibility  VARCHAR(20)  NOT NULL DEFAULT 'PRIVATE'
                             CHECK (visibility IN ('PRIVATE','PUBLIC','FRIENDS')),
    list_type   VARCHAR(30)  CHECK (list_type IN ('WANT_TO_READ','CURRENTLY_READING','COMPLETED','FAVORITES','CUSTOM')),
    cover_image_url VARCHAR(1000),
    sort_order  INT          NOT NULL DEFAULT 0,
    book_count  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reading_list_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: reading_list_item
-- An individual book entry within a reading list
-- ============================================================
CREATE TABLE reading_list_item (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    reading_list_id BIGINT       NOT NULL,
    book_id         BIGINT       NOT NULL,
    added_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    priority        VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
                                 CHECK (priority IN ('LOW','MEDIUM','HIGH')),
    notes           VARCHAR(500),
    completed       BOOLEAN      NOT NULL DEFAULT FALSE,
    completed_at    TIMESTAMP,
    personal_rating INT          CHECK (personal_rating BETWEEN 1 AND 5),
    sort_position   INT          NOT NULL DEFAULT 0,
    CONSTRAINT uq_reading_list_book     UNIQUE (reading_list_id, book_id),
    CONSTRAINT fk_rli_reading_list FOREIGN KEY (reading_list_id) REFERENCES reading_list(id) ON DELETE CASCADE,
    CONSTRAINT fk_rli_book         FOREIGN KEY (book_id)         REFERENCES book(id)
);

-- ============================================================
-- TABLE: reading_list_share
-- Tracks sharing of reading lists between members
-- ============================================================
CREATE TABLE reading_list_share (
    id              BIGINT    AUTO_INCREMENT PRIMARY KEY,
    reading_list_id BIGINT    NOT NULL,
    shared_by_id    BIGINT    NOT NULL,
    shared_with_id  BIGINT    NOT NULL,
    can_edit        BOOLEAN   NOT NULL DEFAULT FALSE,
    shared_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted        BOOLEAN   NOT NULL DEFAULT FALSE,
    accepted_at     TIMESTAMP,
    CONSTRAINT uq_list_share       UNIQUE (reading_list_id, shared_with_id),
    CONSTRAINT fk_share_list       FOREIGN KEY (reading_list_id) REFERENCES reading_list(id) ON DELETE CASCADE,
    CONSTRAINT fk_share_by_member  FOREIGN KEY (shared_by_id)    REFERENCES member(id),
    CONSTRAINT fk_share_with_member FOREIGN KEY (shared_with_id) REFERENCES member(id)
);

-- ============================================================
-- TABLE: saved_search
-- Stored search queries for a member to revisit
-- ============================================================
CREATE TABLE saved_search (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT       NOT NULL,
    name         VARCHAR(200) NOT NULL,
    query        VARCHAR(1000) NOT NULL,
    search_type  VARCHAR(50),
    filters      VARCHAR(1000),
    alert_enabled BOOLEAN     NOT NULL DEFAULT FALSE,
    last_run_at  TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saved_search_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: book_recommendation
-- System-generated or staff-curated book recommendations per member
-- ============================================================
CREATE TABLE book_recommendation (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT       NOT NULL,
    book_id         BIGINT       NOT NULL,
    source          VARCHAR(30)  NOT NULL CHECK (source IN ('ALGORITHM','STAFF','SIMILAR_MEMBERS','GENRE_BASED')),
    score           DECIMAL(5,4),
    reason          VARCHAR(500),
    shown_at        TIMESTAMP,
    clicked         BOOLEAN      NOT NULL DEFAULT FALSE,
    dismissed       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_recommendation UNIQUE (member_id, book_id),
    CONSTRAINT fk_rec_member     FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT fk_rec_book       FOREIGN KEY (book_id)   REFERENCES book(id)
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_reading_list_member          ON reading_list(member_id);
CREATE INDEX idx_reading_list_visibility      ON reading_list(visibility);
CREATE INDEX idx_reading_list_type            ON reading_list(list_type);
CREATE INDEX idx_rli_reading_list_id          ON reading_list_item(reading_list_id);
CREATE INDEX idx_rli_book_id                  ON reading_list_item(book_id);
CREATE INDEX idx_rli_completed                ON reading_list_item(completed);
CREATE INDEX idx_reading_list_share_list      ON reading_list_share(reading_list_id);
CREATE INDEX idx_reading_list_share_with      ON reading_list_share(shared_with_id);
CREATE INDEX idx_saved_search_member          ON saved_search(member_id);
CREATE INDEX idx_book_rec_member              ON book_recommendation(member_id);
CREATE INDEX idx_book_rec_score               ON book_recommendation(member_id, score);
