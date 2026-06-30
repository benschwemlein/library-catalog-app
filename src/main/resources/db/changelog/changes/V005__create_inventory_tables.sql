-- V005: Physical inventory tables
-- Creates book_copy table representing individual physical items
-- Each copy is linked to a catalog record and a branch

-- ============================================================
-- TABLE: book_copy
-- A single physical copy of a book held at a branch
-- ============================================================
CREATE TABLE book_copy (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    book_id         BIGINT        NOT NULL,
    branch_id       BIGINT        NOT NULL,
    barcode         VARCHAR(50)   NOT NULL,
    call_number     VARCHAR(100),
    condition       VARCHAR(20)   NOT NULL DEFAULT 'GOOD'
                                  CHECK (condition IN ('NEW','GOOD','FAIR','POOR','DAMAGED')),
    status          VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE'
                                  CHECK (status IN ('AVAILABLE','CHECKED_OUT','ON_HOLD','LOST','WITHDRAWN','IN_REPAIR','IN_TRANSIT')),
    shelf_location  VARCHAR(100),
    acquired_date   DATE,
    purchase_price  DECIMAL(10,2),
    source          VARCHAR(50)   CHECK (source IN ('PURCHASE','DONATION','GIFT','TRANSFER','ILL')),
    is_reference    BOOLEAN       NOT NULL DEFAULT FALSE,
    can_be_loaned   BOOLEAN       NOT NULL DEFAULT TRUE,
    last_checked_at TIMESTAMP,
    notes           TEXT,
    withdrawn_date  DATE,
    withdrawn_reason VARCHAR(300),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_book_copy_barcode  UNIQUE (barcode),
    CONSTRAINT fk_book_copy_book     FOREIGN KEY (book_id)   REFERENCES book(id),
    CONSTRAINT fk_book_copy_branch   FOREIGN KEY (branch_id) REFERENCES library_branch(id)
);

-- ============================================================
-- TABLE: inventory_check
-- Periodic inventory audit records
-- ============================================================
CREATE TABLE inventory_check (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    branch_id       BIGINT       NOT NULL,
    checked_by      VARCHAR(200) NOT NULL,
    check_date      DATE         NOT NULL,
    total_items     INT          NOT NULL DEFAULT 0,
    items_found     INT          NOT NULL DEFAULT 0,
    items_missing   INT          NOT NULL DEFAULT 0,
    items_damaged   INT          NOT NULL DEFAULT 0,
    notes           TEXT,
    completed       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_check_branch FOREIGN KEY (branch_id) REFERENCES library_branch(id)
);

-- ============================================================
-- TABLE: inventory_check_item
-- Individual copy results from an inventory check
-- ============================================================
CREATE TABLE inventory_check_item (
    id               BIGINT      AUTO_INCREMENT PRIMARY KEY,
    check_id         BIGINT      NOT NULL,
    book_copy_id     BIGINT      NOT NULL,
    found            BOOLEAN     NOT NULL DEFAULT TRUE,
    condition_noted  VARCHAR(20) CHECK (condition_noted IN ('NEW','GOOD','FAIR','POOR','DAMAGED')),
    notes            VARCHAR(500),
    checked_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_item_check FOREIGN KEY (check_id)     REFERENCES inventory_check(id) ON DELETE CASCADE,
    CONSTRAINT fk_inv_item_copy  FOREIGN KEY (book_copy_id) REFERENCES book_copy(id)
);

-- ============================================================
-- TABLE: item_transfer
-- Track transfers of copies between branches
-- ============================================================
CREATE TABLE item_transfer (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    book_copy_id      BIGINT       NOT NULL,
    from_branch_id    BIGINT       NOT NULL,
    to_branch_id      BIGINT       NOT NULL,
    requested_by      VARCHAR(200),
    reason            VARCHAR(300) CHECK (reason IN ('HOLD_FULFILLMENT','REBALANCING','REQUESTED','ILL')),
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                   CHECK (status IN ('PENDING','IN_TRANSIT','COMPLETED','CANCELLED')),
    transfer_date     DATE,
    received_date     DATE,
    notes             VARCHAR(500),
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfer_copy        FOREIGN KEY (book_copy_id)   REFERENCES book_copy(id),
    CONSTRAINT fk_transfer_from_branch FOREIGN KEY (from_branch_id) REFERENCES library_branch(id),
    CONSTRAINT fk_transfer_to_branch   FOREIGN KEY (to_branch_id)   REFERENCES library_branch(id)
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_book_copy_book_id         ON book_copy(book_id);
CREATE INDEX idx_book_copy_branch_id       ON book_copy(branch_id);
CREATE INDEX idx_book_copy_barcode         ON book_copy(barcode);
CREATE INDEX idx_book_copy_status          ON book_copy(status);
CREATE INDEX idx_book_copy_status_branch   ON book_copy(status, branch_id);
CREATE INDEX idx_book_copy_condition       ON book_copy(condition);
CREATE INDEX idx_book_copy_is_reference    ON book_copy(is_reference);
CREATE INDEX idx_inv_check_branch          ON inventory_check(branch_id);
CREATE INDEX idx_inv_check_date            ON inventory_check(check_date);
CREATE INDEX idx_item_transfer_copy        ON item_transfer(book_copy_id);
CREATE INDEX idx_item_transfer_status      ON item_transfer(status);
CREATE INDEX idx_item_transfer_to_branch   ON item_transfer(to_branch_id);
