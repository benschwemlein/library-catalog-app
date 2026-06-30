-- V015: Donation tracking tables
-- Purpose: Track book and item donations from patrons and community members
-- The donation table records the donor information and overall donation batch;
-- donation_item records individual items within each donation.

-- ============================================================
-- TABLE: donation
-- An incoming batch of donated materials from a single donor
-- ============================================================
CREATE TABLE IF NOT EXISTS donation (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    donor_name     VARCHAR(200) NOT NULL,
    donor_email    VARCHAR(255),
    donor_phone    VARCHAR(20),
    donation_date  DATE         NOT NULL,
    branch_id      BIGINT,
    notes          TEXT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('PENDING', 'RECEIVED', 'PROCESSED', 'DECLINED')),
    processed_by   VARCHAR(200),
    processed_date DATE,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_donation_branch FOREIGN KEY (branch_id) REFERENCES library_branch(id)
);

-- ============================================================
-- TABLE: donation_item
-- An individual item (book or other material) within a donation
-- ============================================================
CREATE TABLE IF NOT EXISTS donation_item (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    donation_id      BIGINT       NOT NULL,
    title            VARCHAR(500) NOT NULL,
    author           VARCHAR(300),
    isbn             VARCHAR(20),
    publication_year INT,
    item_condition   VARCHAR(20)
                                  CHECK (item_condition IN ('EXCELLENT', 'GOOD', 'FAIR', 'POOR')),
    quantity         INT          NOT NULL DEFAULT 1,
    action           VARCHAR(20)
                                  CHECK (action IN ('ACCEPTED', 'REJECTED', 'ADDED_TO_CATALOG', 'PENDING_REVIEW')),
    book_id          BIGINT,
    notes            VARCHAR(500),
    reviewed_at      TIMESTAMP,
    CONSTRAINT fk_donation_item_donation FOREIGN KEY (donation_id) REFERENCES donation(id) ON DELETE CASCADE,
    CONSTRAINT fk_donation_item_book     FOREIGN KEY (book_id)     REFERENCES book(id)
);

-- ============================================================
-- Indexes for common query patterns
-- ============================================================

-- Look up donations by branch (e.g. "all donations received at North Branch")
CREATE INDEX IF NOT EXISTS idx_donation_branch       ON donation(branch_id);

-- Filter donations by processing status
CREATE INDEX IF NOT EXISTS idx_donation_status       ON donation(status);

-- Date-range queries on donations
CREATE INDEX IF NOT EXISTS idx_donation_date         ON donation(donation_date);

-- All items belonging to a donation batch
CREATE INDEX IF NOT EXISTS idx_donation_item_donation ON donation_item(donation_id);

-- Find the catalog book linked to a donated item
CREATE INDEX IF NOT EXISTS idx_donation_item_book     ON donation_item(book_id);

-- Filter donation items by review action taken
CREATE INDEX IF NOT EXISTS idx_donation_item_action   ON donation_item(action);

-- Filter donation items by physical condition
CREATE INDEX IF NOT EXISTS idx_donation_item_condition ON donation_item(item_condition);

-- Donor email lookup (for donor history queries)
CREATE INDEX IF NOT EXISTS idx_donation_donor_email   ON donation(donor_email);

-- Processed-by staff lookup
CREATE INDEX IF NOT EXISTS idx_donation_processed_by  ON donation(processed_by);

-- Date range on processed_date for reporting
CREATE INDEX IF NOT EXISTS idx_donation_processed_date ON donation(processed_date);
