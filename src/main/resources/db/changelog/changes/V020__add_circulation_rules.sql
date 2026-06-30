-- V020: Circulation rules engine and batch job tracking
-- Purpose: Define loan periods, renewal limits, fine rates, and item-count
--          caps per membership tier and item type.  Also creates the
--          batch_job table used by scheduled administrative jobs.

-- ============================================================
-- TABLE: circulation_rule
-- Each row defines the checkout policy for a membership-tier/item-type
-- combination.  branch_id = NULL means the rule applies globally;
-- a non-null branch_id overrides the global rule for that branch.
-- ============================================================
CREATE TABLE IF NOT EXISTS circulation_rule (
    id                   BIGINT        AUTO_INCREMENT PRIMARY KEY,
    membership_tier      VARCHAR(20)
                                       CHECK (membership_tier IN ('STANDARD', 'PREMIUM', 'STUDENT')),
    item_type            VARCHAR(20)   NOT NULL
                                       CHECK (item_type IN ('BOOK', 'REFERENCE', 'AUDIOVISUAL', 'PERIODICAL', 'DIGITAL')),
    branch_id            BIGINT,
    loan_period_days     INT           NOT NULL DEFAULT 21,
    max_renewals         INT           NOT NULL DEFAULT 2,
    fine_rate_per_day    DECIMAL(5,2)  NOT NULL DEFAULT 0.25,
    max_fine_amount      DECIMAL(10,2),
    max_loans_allowed    INT           NOT NULL DEFAULT 8,
    reservation_hold_days INT          NOT NULL DEFAULT 7,
    min_age_required     INT                    DEFAULT 0,
    active               BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_circulation_rule UNIQUE (membership_tier, item_type, branch_id),
    CONSTRAINT fk_circ_rule_branch FOREIGN KEY (branch_id) REFERENCES library_branch(id)
);

-- ============================================================
-- Global rules: branch_id = NULL (applies to all branches)
-- ============================================================

-- STANDARD tier — everyday member with standard borrowing privileges
INSERT INTO circulation_rule
    (membership_tier, item_type, branch_id, loan_period_days, max_renewals,
     fine_rate_per_day, max_fine_amount, max_loans_allowed, reservation_hold_days)
VALUES
    ('STANDARD', 'BOOK',        NULL, 21,  2, 0.25, 25.00, 8,  7),
    ('STANDARD', 'REFERENCE',   NULL, 7,   0, 1.00, NULL,  2,  3),
    ('STANDARD', 'AUDIOVISUAL', NULL, 14,  1, 0.50, 20.00, 4,  5),
    ('STANDARD', 'PERIODICAL',  NULL, 7,   0, 0.50, 10.00, 4,  3),
    ('STANDARD', 'DIGITAL',     NULL, 14,  1, 0.00, 0.00,  5,  0);

-- PREMIUM tier — paid annual membership with extended privileges
INSERT INTO circulation_rule
    (membership_tier, item_type, branch_id, loan_period_days, max_renewals,
     fine_rate_per_day, max_fine_amount, max_loans_allowed, reservation_hold_days)
VALUES
    ('PREMIUM',  'BOOK',        NULL, 28,  3, 0.10, 10.00, 12, 7),
    ('PREMIUM',  'REFERENCE',   NULL, 14,  1, 0.50, NULL,  4,  5),
    ('PREMIUM',  'AUDIOVISUAL', NULL, 21,  2, 0.25, 15.00, 6,  7),
    ('PREMIUM',  'PERIODICAL',  NULL, 14,  1, 0.25, 8.00,  6,  5),
    ('PREMIUM',  'DIGITAL',     NULL, 21,  2, 0.00, 0.00,  10, 0);

-- STUDENT tier — reduced-cost membership for enrolled students
INSERT INTO circulation_rule
    (membership_tier, item_type, branch_id, loan_period_days, max_renewals,
     fine_rate_per_day, max_fine_amount, max_loans_allowed, reservation_hold_days)
VALUES
    ('STUDENT',  'BOOK',        NULL, 14,  1, 0.15, 15.00, 5,  7),
    ('STUDENT',  'REFERENCE',   NULL, 3,   0, 1.00, NULL,  2,  2),
    ('STUDENT',  'AUDIOVISUAL', NULL, 7,   0, 0.50, 10.00, 2,  3),
    ('STUDENT',  'PERIODICAL',  NULL, 7,   0, 0.25, 5.00,  3,  3),
    ('STUDENT',  'DIGITAL',     NULL, 14,  1, 0.00, 0.00,  5,  0);

-- ============================================================
-- Indexes on circulation_rule
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_circ_rule_tier   ON circulation_rule(membership_tier);
CREATE INDEX IF NOT EXISTS idx_circ_rule_type   ON circulation_rule(item_type);
CREATE INDEX IF NOT EXISTS idx_circ_rule_branch ON circulation_rule(branch_id);
CREATE INDEX IF NOT EXISTS idx_circ_rule_active ON circulation_rule(active);

-- ============================================================
-- TABLE: batch_job
-- Tracks the execution history of scheduled administrative jobs.
-- Each job run (whether triggered by a scheduler or manually) gets
-- one row so operations staff can audit outcomes and diagnose failures.
-- ============================================================
CREATE TABLE IF NOT EXISTS batch_job (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    job_type          VARCHAR(50)  NOT NULL
                                   CHECK (job_type IN (
                                       'OVERDUE_PROCESSING',
                                       'HOLD_EXPIRY',
                                       'MEMBERSHIP_RENEWAL_NOTICES',
                                       'FINE_COLLECTION_REPORT',
                                       'CATALOG_SYNC',
                                       'MEMBER_CLEANUP'
                                   )),
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                   CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    started_at        TIMESTAMP,
    completed_at      TIMESTAMP,
    records_processed INT                   DEFAULT 0,
    records_failed    INT                   DEFAULT 0,
    error_message     TEXT,
    triggered_by      VARCHAR(200),
    parameters        TEXT,
    created_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: batch_job_step
-- Individual processing steps within a batch job.
-- Provides granular progress tracking for long-running jobs.
-- ============================================================
CREATE TABLE IF NOT EXISTS batch_job_step (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    job_id            BIGINT       NOT NULL,
    step_name         VARCHAR(100) NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                   CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'SKIPPED')),
    started_at        TIMESTAMP,
    completed_at      TIMESTAMP,
    items_read        INT                   DEFAULT 0,
    items_written     INT                   DEFAULT 0,
    items_skipped     INT                   DEFAULT 0,
    items_failed      INT                   DEFAULT 0,
    error_message     TEXT,
    created_at        TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_step_job FOREIGN KEY (job_id) REFERENCES batch_job(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes on batch_job and batch_job_step
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_batch_job_status    ON batch_job(status);
CREATE INDEX IF NOT EXISTS idx_batch_job_type      ON batch_job(job_type);
CREATE INDEX IF NOT EXISTS idx_batch_job_created   ON batch_job(created_at);
CREATE INDEX IF NOT EXISTS idx_batch_job_type_status ON batch_job(job_type, status);
CREATE INDEX IF NOT EXISTS idx_batch_step_job      ON batch_job_step(job_id);
CREATE INDEX IF NOT EXISTS idx_batch_step_status   ON batch_job_step(status);
