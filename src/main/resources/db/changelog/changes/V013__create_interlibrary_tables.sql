-- V013: Interlibrary loan tables
-- Creates partner_library and inter_library_loan_request tables
-- Supports borrowing items from and lending to other library systems

-- ============================================================
-- TABLE: partner_library
-- External libraries that participate in ILL agreements
-- ============================================================
CREATE TABLE partner_library (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(300) NOT NULL,
    code            VARCHAR(20)  NOT NULL,
    system_type     VARCHAR(50),
    address         VARCHAR(500),
    city            VARCHAR(100),
    state           VARCHAR(50),
    zip             VARCHAR(20),
    country         VARCHAR(100) DEFAULT 'USA',
    phone           VARCHAR(30),
    fax             VARCHAR(30),
    email           VARCHAR(255),
    contact_name    VARCHAR(200),
    contact_email   VARCHAR(255),
    ils_platform    VARCHAR(100),
    oclc_number     VARCHAR(50),
    loan_period_days INT          NOT NULL DEFAULT 21,
    renewal_allowed  BOOLEAN      NOT NULL DEFAULT TRUE,
    shipping_method  VARCHAR(100),
    reciprocal       BOOLEAN      NOT NULL DEFAULT TRUE,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    notes            TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_partner_library_code UNIQUE (code)
);

-- ============================================================
-- TABLE: inter_library_loan_request
-- A request to borrow a title from or lend to a partner library
-- ============================================================
CREATE TABLE inter_library_loan_request (
    id                    BIGINT       AUTO_INCREMENT PRIMARY KEY,
    direction             VARCHAR(10)  NOT NULL DEFAULT 'BORROW'
                                       CHECK (direction IN ('BORROW','LEND')),
    requesting_member_id  BIGINT,
    requesting_branch_id  BIGINT,
    lending_branch_id     BIGINT,
    book_title            VARCHAR(500) NOT NULL,
    book_author           VARCHAR(300),
    isbn                  VARCHAR(20),
    publisher             VARCHAR(200),
    publication_year      INT,
    edition               VARCHAR(50),
    volume_issue          VARCHAR(100),
    article_title         VARCHAR(500),
    pages                 VARCHAR(50),
    notes                 TEXT,
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                       CHECK (status IN ('PENDING','SUBMITTED','APPROVED','RECEIVED','RETURNED','OVERDUE','DENIED','CANCELLED','LENT_OUT')),
    partner_library_id    BIGINT,
    requested_date        DATE         NOT NULL DEFAULT CURRENT_DATE,
    needed_by_date        DATE,
    submitted_date        DATE,
    confirmed_date        DATE,
    received_date         DATE,
    due_date              DATE,
    returned_date         DATE,
    lender_reference      VARCHAR(100),
    borrower_reference    VARCHAR(100),
    condition_on_receipt  VARCHAR(20)  CHECK (condition_on_receipt IN ('GOOD','FAIR','POOR','DAMAGED')),
    handling_notes        VARCHAR(500),
    book_copy_id          BIGINT,
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ill_member          FOREIGN KEY (requesting_member_id) REFERENCES member(id),
    CONSTRAINT fk_ill_req_branch      FOREIGN KEY (requesting_branch_id) REFERENCES library_branch(id),
    CONSTRAINT fk_ill_lend_branch     FOREIGN KEY (lending_branch_id)    REFERENCES library_branch(id),
    CONSTRAINT fk_ill_partner_library FOREIGN KEY (partner_library_id)   REFERENCES partner_library(id),
    CONSTRAINT fk_ill_book_copy       FOREIGN KEY (book_copy_id)         REFERENCES book_copy(id)
);

-- ============================================================
-- TABLE: ill_communication
-- Messages exchanged between libraries for an ILL request
-- ============================================================
CREATE TABLE ill_communication (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    request_id  BIGINT       NOT NULL,
    direction   VARCHAR(10)  NOT NULL CHECK (direction IN ('SENT','RECEIVED')),
    message     TEXT         NOT NULL,
    sent_by     VARCHAR(200),
    sent_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ill_comm_request FOREIGN KEY (request_id) REFERENCES inter_library_loan_request(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_partner_library_code         ON partner_library(code);
CREATE INDEX idx_partner_library_active       ON partner_library(active);
CREATE INDEX idx_ill_request_status           ON inter_library_loan_request(status);
CREATE INDEX idx_ill_request_member           ON inter_library_loan_request(requesting_member_id);
CREATE INDEX idx_ill_request_partner          ON inter_library_loan_request(partner_library_id);
CREATE INDEX idx_ill_request_date             ON inter_library_loan_request(requested_date);
CREATE INDEX idx_ill_request_due_date         ON inter_library_loan_request(due_date);
CREATE INDEX idx_ill_request_direction        ON inter_library_loan_request(direction);
CREATE INDEX idx_ill_comm_request             ON ill_communication(request_id);
