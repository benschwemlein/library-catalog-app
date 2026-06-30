-- V012: Periodical tables
-- Creates periodical, periodical_issue, and periodical_subscription tables
-- Manages newspapers, journals, and magazines in the collection

-- ============================================================
-- TABLE: periodical
-- A serialized publication (newspaper, journal, magazine)
-- ============================================================
CREATE TABLE periodical (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(500) NOT NULL,
    issn            VARCHAR(20)  UNIQUE,
    eissn           VARCHAR(20),
    publisher_id    BIGINT,
    frequency       VARCHAR(30)  NOT NULL
                                 CHECK (frequency IN ('DAILY','WEEKLY','BIWEEKLY','MONTHLY','BIMONTHLY','QUARTERLY','SEMIANNUAL','ANNUAL','IRREGULAR')),
    description     TEXT,
    subject_area    VARCHAR(200),
    language        VARCHAR(50)  NOT NULL DEFAULT 'English',
    country_of_pub  VARCHAR(100),
    start_year      INT,
    end_year        INT,
    cover_image_url VARCHAR(1000),
    dewey_class     VARCHAR(50),
    peer_reviewed   BOOLEAN      NOT NULL DEFAULT FALSE,
    digital_access  BOOLEAN      NOT NULL DEFAULT FALSE,
    digital_url     VARCHAR(1000),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_periodical_publisher FOREIGN KEY (publisher_id) REFERENCES publisher(id)
);

-- ============================================================
-- TABLE: periodical_issue
-- A specific issue/edition of a periodical held at a branch
-- ============================================================
CREATE TABLE periodical_issue (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    periodical_id    BIGINT       NOT NULL,
    volume           INT,
    issue_number     INT,
    supplement       VARCHAR(50),
    publication_date DATE         NOT NULL,
    title            VARCHAR(500),
    cover_story      VARCHAR(500),
    status           VARCHAR(20)  NOT NULL DEFAULT 'EXPECTED'
                                   CHECK (status IN ('EXPECTED','RECEIVED','MISSING','WITHDRAWN','ARCHIVED')),
    received_date    DATE,
    barcode          VARCHAR(50),
    branch_id        BIGINT       NOT NULL,
    location         VARCHAR(100),
    condition        VARCHAR(20)  CHECK (condition IN ('NEW','GOOD','FAIR','POOR','DAMAGED')),
    checked_out      BOOLEAN      NOT NULL DEFAULT FALSE,
    notes            TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_periodical_issue_barcode UNIQUE (barcode),
    CONSTRAINT fk_issue_periodical  FOREIGN KEY (periodical_id) REFERENCES periodical(id)       ON DELETE CASCADE,
    CONSTRAINT fk_issue_branch      FOREIGN KEY (branch_id)     REFERENCES library_branch(id)
);

-- ============================================================
-- TABLE: periodical_subscription
-- Active subscriptions maintained per branch for a periodical
-- ============================================================
CREATE TABLE periodical_subscription (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    periodical_id   BIGINT        NOT NULL,
    branch_id       BIGINT        NOT NULL,
    start_date      DATE          NOT NULL,
    end_date        DATE,
    cost_per_year   DECIMAL(10,2),
    vendor          VARCHAR(200),
    vendor_account  VARCHAR(100),
    account_number  VARCHAR(100),
    auto_renew      BOOLEAN       NOT NULL DEFAULT TRUE,
    issues_per_year INT,
    notes           TEXT,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sub_periodical FOREIGN KEY (periodical_id) REFERENCES periodical(id),
    CONSTRAINT fk_sub_branch     FOREIGN KEY (branch_id)     REFERENCES library_branch(id)
);

-- ============================================================
-- TABLE: periodical_article
-- Individual article metadata within a periodical issue
-- (for databases and indexed journals)
-- ============================================================
CREATE TABLE periodical_article (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    issue_id         BIGINT       NOT NULL,
    title            VARCHAR(500) NOT NULL,
    author_names     VARCHAR(500),
    start_page       INT,
    end_page         INT,
    abstract         TEXT,
    keywords         VARCHAR(1000),
    doi              VARCHAR(200),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_article_issue FOREIGN KEY (issue_id) REFERENCES periodical_issue(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_periodical_issn             ON periodical(issn);
CREATE INDEX idx_periodical_publisher        ON periodical(publisher_id);
CREATE INDEX idx_periodical_active           ON periodical(active);
CREATE INDEX idx_periodical_frequency        ON periodical(frequency);
CREATE INDEX idx_periodical_subject          ON periodical(subject_area);
CREATE INDEX idx_issue_periodical_id         ON periodical_issue(periodical_id);
CREATE INDEX idx_issue_branch_id             ON periodical_issue(branch_id);
CREATE INDEX idx_issue_status                ON periodical_issue(status);
CREATE INDEX idx_issue_pub_date              ON periodical_issue(publication_date);
CREATE INDEX idx_issue_barcode               ON periodical_issue(barcode);
CREATE INDEX idx_subscription_periodical     ON periodical_subscription(periodical_id);
CREATE INDEX idx_subscription_branch         ON periodical_subscription(branch_id);
CREATE INDEX idx_subscription_active         ON periodical_subscription(active);
CREATE INDEX idx_subscription_end_date       ON periodical_subscription(end_date);
CREATE INDEX idx_article_issue_id            ON periodical_article(issue_id);
