-- V008: Digital resource tables
-- Creates digital_resource, digital_license, and digital_loan tables
-- Supports ebooks, audiobooks, streaming video, and database access

-- ============================================================
-- TABLE: digital_resource
-- Metadata for a digital content item
-- ============================================================
CREATE TABLE digital_resource (
    id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(500)  NOT NULL,
    subtitle          VARCHAR(500),
    isbn              VARCHAR(20),
    publisher         VARCHAR(300),
    author_names      VARCHAR(500),
    type              VARCHAR(30)   NOT NULL
                                    CHECK (type IN ('EBOOK','AUDIOBOOK','VIDEO','ARTICLE','DATABASE','MAGAZINE','NEWSPAPER')),
    format            VARCHAR(20)   CHECK (format IN ('EPUB','PDF','MP3','M4B','MP4','STREAMING','ONLINE')),
    file_url          VARCHAR(1000),
    thumbnail_url     VARCHAR(1000),
    description       TEXT,
    publication_year  INT,
    language          VARCHAR(50)   NOT NULL DEFAULT 'English',
    file_size_bytes   BIGINT,
    duration_seconds  INT,
    page_count        INT,
    narrator          VARCHAR(300),
    audience_level    VARCHAR(30)   CHECK (audience_level IN ('CHILDREN','YOUNG_ADULT','ADULT','PROFESSIONAL')),
    content_rating    VARCHAR(20),
    genre_names       VARCHAR(500),
    subject_headings  VARCHAR(1000),
    external_id       VARCHAR(200),
    vendor            VARCHAR(200),
    active            BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: digital_license
-- License terms for accessing a digital resource
-- A resource may have multiple license agreements (e.g. different vendors)
-- ============================================================
CREATE TABLE digital_license (
    id                   BIGINT        AUTO_INCREMENT PRIMARY KEY,
    resource_id          BIGINT        NOT NULL,
    license_type         VARCHAR(30)   NOT NULL
                                       CHECK (license_type IN ('SIMULTANEOUS_USER','ONE_AT_A_TIME','UNLIMITED','METERED')),
    max_concurrent_loans INT,
    loan_period_days     INT           NOT NULL DEFAULT 14,
    cost_per_loan        DECIMAL(10,2),
    annual_cost          DECIMAL(12,2),
    vendor_name          VARCHAR(200)  NOT NULL,
    vendor_account_id    VARCHAR(100),
    license_key          VARCHAR(500),
    platform_url         VARCHAR(1000),
    expires_at           DATE,
    start_date           DATE,
    auto_renew           BOOLEAN       NOT NULL DEFAULT FALSE,
    active               BOOLEAN       NOT NULL DEFAULT TRUE,
    notes                TEXT,
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_digital_license_resource FOREIGN KEY (resource_id) REFERENCES digital_resource(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: digital_loan
-- A checkout/access event for a digital resource by a member
-- ============================================================
CREATE TABLE digital_loan (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    resource_id     BIGINT        NOT NULL,
    member_id       BIGINT        NOT NULL,
    license_id      BIGINT,
    checked_out_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_at          TIMESTAMP     NOT NULL,
    returned_at     TIMESTAMP,
    extended_at     TIMESTAMP,
    status          VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
                                  CHECK (status IN ('ACTIVE','RETURNED','EXPIRED','REVOKED')),
    access_token    VARCHAR(512),
    device_info     VARCHAR(500),
    download_count  INT           NOT NULL DEFAULT 0,
    last_accessed   TIMESTAMP,
    ip_address      VARCHAR(50),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_digital_loan_resource FOREIGN KEY (resource_id) REFERENCES digital_resource(id),
    CONSTRAINT fk_digital_loan_member   FOREIGN KEY (member_id)   REFERENCES member(id),
    CONSTRAINT fk_digital_loan_license  FOREIGN KEY (license_id)  REFERENCES digital_license(id)
);

-- ============================================================
-- TABLE: digital_hold
-- Queue for digital resources with limited concurrent users
-- ============================================================
CREATE TABLE digital_hold (
    id              BIGINT    AUTO_INCREMENT PRIMARY KEY,
    resource_id     BIGINT    NOT NULL,
    member_id       BIGINT    NOT NULL,
    placed_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notified_at     TIMESTAMP,
    expires_at      TIMESTAMP,
    status          VARCHAR(20) NOT NULL DEFAULT 'WAITING'
                                CHECK (status IN ('WAITING','READY','FULFILLED','CANCELLED','EXPIRED')),
    queue_position  INT       NOT NULL DEFAULT 1,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_digital_hold_resource FOREIGN KEY (resource_id) REFERENCES digital_resource(id),
    CONSTRAINT fk_digital_hold_member   FOREIGN KEY (member_id)   REFERENCES member(id)
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_digital_resource_type       ON digital_resource(type);
CREATE INDEX idx_digital_resource_active     ON digital_resource(active);
CREATE INDEX idx_digital_resource_title      ON digital_resource(title);
CREATE INDEX idx_digital_resource_isbn       ON digital_resource(isbn);
CREATE INDEX idx_digital_license_resource    ON digital_license(resource_id);
CREATE INDEX idx_digital_license_active      ON digital_license(active);
CREATE INDEX idx_digital_license_expires     ON digital_license(expires_at);
CREATE INDEX idx_digital_loan_resource       ON digital_loan(resource_id);
CREATE INDEX idx_digital_loan_member         ON digital_loan(member_id);
CREATE INDEX idx_digital_loan_status         ON digital_loan(status);
CREATE INDEX idx_digital_loan_due_at         ON digital_loan(due_at);
CREATE INDEX idx_digital_hold_resource       ON digital_hold(resource_id);
CREATE INDEX idx_digital_hold_member         ON digital_hold(member_id);
CREATE INDEX idx_digital_hold_status         ON digital_hold(status);
