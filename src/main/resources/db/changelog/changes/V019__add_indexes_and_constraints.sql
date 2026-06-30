-- V019: Additional performance indexes and constraints
-- Purpose: Add composite and single-column indexes across all high-traffic
--          tables to support the query patterns used by service layer methods.
--          Also creates the search_index table used for full-text search caching.

-- ============================================================
-- LOAN indexes
-- Supports overdue sweeps, per-member loan lists, and checkout history.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_loan_due_date         ON loan(due_date);
CREATE INDEX IF NOT EXISTS idx_loan_member_status    ON loan(member_id, status);
CREATE INDEX IF NOT EXISTS idx_loan_status           ON loan(status);
CREATE INDEX IF NOT EXISTS idx_loan_checkout_date    ON loan(checkout_date);
CREATE INDEX IF NOT EXISTS idx_loan_copy             ON loan(book_copy_id);

-- ============================================================
-- HOLD indexes
-- Supports queue processing, per-member hold lists, and expiry sweeps.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_hold_status_expiry    ON hold(status, expiry_date);
CREATE INDEX IF NOT EXISTS idx_hold_member           ON hold(member_id);
CREATE INDEX IF NOT EXISTS idx_hold_book             ON hold(book_id);
CREATE INDEX IF NOT EXISTS idx_hold_branch           ON hold(pickup_branch_id);
CREATE INDEX IF NOT EXISTS idx_hold_placed_date      ON hold(placed_date);

-- ============================================================
-- FINE indexes
-- Supports unpaid-balance checks and per-member fine history.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_fine_paid             ON fine(paid);
CREATE INDEX IF NOT EXISTS idx_fine_member           ON fine(member_id);
CREATE INDEX IF NOT EXISTS idx_fine_issued_date      ON fine(issued_date);
CREATE INDEX IF NOT EXISTS idx_fine_loan             ON fine(loan_id);

-- ============================================================
-- MEMBER indexes
-- Supports membership searches, expiry sweeps, and tier-based queries.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_member_fine_balance   ON member(fine_balance);
CREATE INDEX IF NOT EXISTS idx_member_active         ON member(active);
CREATE INDEX IF NOT EXISTS idx_member_tier           ON member(membership_tier);
CREATE INDEX IF NOT EXISTS idx_member_expiry         ON member(expiry_date);
CREATE INDEX IF NOT EXISTS idx_member_membership_number ON member(membership_number);

-- ============================================================
-- BOOK COPY indexes
-- Supports availability lookups and per-branch inventory views.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_book_copy_status_branch ON book_copy(status, branch_id);
CREATE INDEX IF NOT EXISTS idx_book_copy_barcode      ON book_copy(barcode);
CREATE INDEX IF NOT EXISTS idx_book_copy_book         ON book_copy(book_id);
CREATE INDEX IF NOT EXISTS idx_book_copy_status       ON book_copy(status);

-- ============================================================
-- BOOK indexes
-- Supports catalog search by ISBN, title, and publication year.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_book_isbn             ON book(isbn);
CREATE INDEX IF NOT EXISTS idx_book_title            ON book(title);
CREATE INDEX IF NOT EXISTS idx_book_pub_year         ON book(publication_year);

-- ============================================================
-- AUTHOR indexes
-- Supports author name searches and alphabetical listings.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_author_last_name      ON author(last_name);
CREATE INDEX IF NOT EXISTS idx_author_full_name      ON author(first_name, last_name);

-- ============================================================
-- NOTIFICATION indexes
-- Supports unread-count queries and notification history feeds.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_notification_member_read ON notification(member_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notification_type        ON notification(type);
CREATE INDEX IF NOT EXISTS idx_notification_sent_at     ON notification(sent_at);

-- ============================================================
-- DIGITAL LOAN indexes
-- Supports active-loan counts per resource and member dashboards.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_digital_loan_status   ON digital_loan(status);
CREATE INDEX IF NOT EXISTS idx_digital_loan_member   ON digital_loan(member_id);

-- ============================================================
-- SEARCH LOG and AUDIT LOG indexes
-- Supports analytics dashboards and audit trail queries.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_search_log_at         ON search_log(searched_at);
CREATE INDEX IF NOT EXISTS idx_audit_entity          ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_performed_at    ON audit_log(performed_at);

-- ============================================================
-- BOOK CLUB indexes
-- Supports club roster queries and active-membership counts.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_club_membership_active ON book_club_membership(club_id, active);

-- ============================================================
-- CHALLENGE PARTICIPATION indexes
-- Supports leaderboard queries and completion-rate statistics.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_challenge_participation_completed ON challenge_participation(challenge_id, completed);

-- ============================================================
-- DIGITAL RESOURCE indexes
-- Supports type-filtered browsing and availability checks.
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_digital_resource_type   ON digital_resource(type);
CREATE INDEX IF NOT EXISTS idx_digital_resource_active ON digital_resource(active);

-- ============================================================
-- TABLE: search_index
-- Denormalised search cache used by the full-text search service.
-- One row per searchable entity (BOOK, MEMBER, AUTHOR).
-- The content column holds concatenated, normalised search tokens.
-- ============================================================
CREATE TABLE IF NOT EXISTS search_index (
    id           BIGINT        AUTO_INCREMENT PRIMARY KEY,
    entity_type  VARCHAR(20)   NOT NULL
                               CHECK (entity_type IN ('BOOK', 'MEMBER', 'AUTHOR')),
    entity_id    BIGINT        NOT NULL,
    title        VARCHAR(500)  NOT NULL,
    content      TEXT,
    tags         VARCHAR(1000),
    last_indexed TIMESTAMP,
    boost        FLOAT         DEFAULT 1.0,
    entity_url   VARCHAR(500),
    CONSTRAINT uq_search_index_entity UNIQUE (entity_type, entity_id)
);

-- Supports type-scoped searches
CREATE INDEX IF NOT EXISTS idx_search_index_type         ON search_index(entity_type);

-- Supports incremental re-indexing jobs (find stale entries)
CREATE INDEX IF NOT EXISTS idx_search_index_last_indexed ON search_index(last_indexed);

-- Supports relevance-ranked results (higher boost = higher in results)
CREATE INDEX IF NOT EXISTS idx_search_index_boost        ON search_index(boost);
