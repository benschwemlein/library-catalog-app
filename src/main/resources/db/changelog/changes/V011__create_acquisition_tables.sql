-- V011: Acquisition tables
-- Creates acquisition_request and purchase_order tables
-- Supports the library's collection development workflow

-- ============================================================
-- TABLE: purchase_order
-- An order placed with a book vendor
-- (defined before acquisition_request so FK can reference it)
-- ============================================================
CREATE TABLE purchase_order (
    id                     BIGINT        AUTO_INCREMENT PRIMARY KEY,
    order_number           VARCHAR(50)   NOT NULL,
    vendor_name            VARCHAR(200)  NOT NULL,
    vendor_contact         VARCHAR(200),
    vendor_email           VARCHAR(255),
    vendor_phone           VARCHAR(30),
    order_date             DATE          NOT NULL,
    expected_delivery_date DATE,
    actual_delivery_date   DATE,
    total_amount           DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status                 VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
                                         CHECK (status IN ('DRAFT','SUBMITTED','CONFIRMED','SHIPPED','RECEIVED','CANCELLED','PARTIALLY_RECEIVED')),
    shipping_address       VARCHAR(500),
    branch_id              BIGINT,
    invoice_number         VARCHAR(100),
    notes                  TEXT,
    created_by             VARCHAR(200)  NOT NULL,
    approved_by            VARCHAR(200),
    approved_date          DATE,
    created_at             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_purchase_order_number UNIQUE (order_number),
    CONSTRAINT fk_po_branch FOREIGN KEY (branch_id) REFERENCES library_branch(id)
);

-- ============================================================
-- TABLE: acquisition_request
-- A request to acquire a new title for the collection
-- (from staff or member suggestion)
-- ============================================================
CREATE TABLE acquisition_request (
    id                       BIGINT       AUTO_INCREMENT PRIMARY KEY,
    requested_by_member_id   BIGINT,
    requested_by_staff       VARCHAR(200),
    title                    VARCHAR(500) NOT NULL,
    author                   VARCHAR(300),
    isbn                     VARCHAR(20),
    publisher                VARCHAR(300),
    publication_year         INT,
    edition                  VARCHAR(50),
    format                   VARCHAR(30)  CHECK (format IN ('HARDCOVER','PAPERBACK','EBOOK','AUDIOBOOK','LARGE_PRINT')),
    reason                   TEXT,
    priority                 VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
                                          CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    status                   VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                          CHECK (status IN ('PENDING','APPROVED','DENIED','ORDERED','RECEIVED','CANCELLED')),
    request_date             DATE         NOT NULL DEFAULT CURRENT_DATE,
    reviewed_date            DATE,
    reviewed_by              VARCHAR(200),
    review_notes             TEXT,
    target_branch_id         BIGINT,
    purchase_order_id        BIGINT,
    estimated_cost           DECIMAL(10,2),
    actual_cost              DECIMAL(10,2),
    quantity_requested       INT          NOT NULL DEFAULT 1,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_acq_member         FOREIGN KEY (requested_by_member_id) REFERENCES member(id),
    CONSTRAINT fk_acq_target_branch  FOREIGN KEY (target_branch_id)       REFERENCES library_branch(id),
    CONSTRAINT fk_acq_purchase_order FOREIGN KEY (purchase_order_id)      REFERENCES purchase_order(id)
);

-- ============================================================
-- TABLE: purchase_order_line
-- Individual line items on a purchase order
-- ============================================================
CREATE TABLE purchase_order_line (
    id                    BIGINT        AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id     BIGINT        NOT NULL,
    acquisition_request_id BIGINT,
    title                 VARCHAR(500)  NOT NULL,
    author                VARCHAR(300),
    isbn                  VARCHAR(20),
    publisher             VARCHAR(300),
    quantity              INT           NOT NULL DEFAULT 1,
    unit_price            DECIMAL(10,2),
    line_total            DECIMAL(10,2),
    received_quantity     INT           NOT NULL DEFAULT 0,
    status                VARCHAR(20)   CHECK (status IN ('ORDERED','PARTIALLY_RECEIVED','RECEIVED','CANCELLED','BACKORDERED')),
    notes                 VARCHAR(500),
    created_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pol_order   FOREIGN KEY (purchase_order_id)      REFERENCES purchase_order(id)     ON DELETE CASCADE,
    CONSTRAINT fk_pol_request FOREIGN KEY (acquisition_request_id) REFERENCES acquisition_request(id)
);

-- ============================================================
-- TABLE: vendor
-- Book vendors and distributors
-- ============================================================
CREATE TABLE vendor (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(300) NOT NULL,
    code         VARCHAR(50)  UNIQUE,
    contact_name VARCHAR(200),
    email        VARCHAR(255),
    phone        VARCHAR(30),
    address      VARCHAR(500),
    website      VARCHAR(500),
    terms        VARCHAR(500),
    discount_rate DECIMAL(5,2),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    notes        TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_acq_request_status          ON acquisition_request(status);
CREATE INDEX idx_acq_request_priority        ON acquisition_request(priority);
CREATE INDEX idx_acq_request_member          ON acquisition_request(requested_by_member_id);
CREATE INDEX idx_acq_request_branch          ON acquisition_request(target_branch_id);
CREATE INDEX idx_acq_request_po              ON acquisition_request(purchase_order_id);
CREATE INDEX idx_acq_request_date            ON acquisition_request(request_date);
CREATE INDEX idx_purchase_order_status       ON purchase_order(status);
CREATE INDEX idx_purchase_order_date         ON purchase_order(order_date);
CREATE INDEX idx_purchase_order_branch       ON purchase_order(branch_id);
CREATE INDEX idx_pol_order                   ON purchase_order_line(purchase_order_id);
CREATE INDEX idx_pol_request                 ON purchase_order_line(acquisition_request_id);
