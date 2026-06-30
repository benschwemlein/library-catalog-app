-- V006: Circulation tables
-- Creates loan, hold, and fine tables
-- Core of the library's borrowing and reservation system

-- ============================================================
-- TABLE: loan
-- Records each checkout of a physical book copy
-- ============================================================
CREATE TABLE loan (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    book_copy_id      BIGINT       NOT NULL,
    member_id         BIGINT       NOT NULL,
    branch_id         BIGINT       NOT NULL,
    checkout_date     TIMESTAMP    NOT NULL,
    due_date          TIMESTAMP    NOT NULL,
    return_date       TIMESTAMP,
    renewal_count     INT          NOT NULL DEFAULT 0,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                                   CHECK (status IN ('ACTIVE','RETURNED','OVERDUE','LOST')),
    notes             TEXT,
    checkout_staff_id BIGINT,
    return_staff_id   BIGINT,
    auto_renewed      BOOLEAN      NOT NULL DEFAULT FALSE,
    email_reminder_sent BOOLEAN    NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_book_copy       FOREIGN KEY (book_copy_id)      REFERENCES book_copy(id),
    CONSTRAINT fk_loan_member          FOREIGN KEY (member_id)         REFERENCES member(id),
    CONSTRAINT fk_loan_branch          FOREIGN KEY (branch_id)         REFERENCES library_branch(id),
    CONSTRAINT fk_loan_checkout_staff  FOREIGN KEY (checkout_staff_id) REFERENCES staff_member(id),
    CONSTRAINT fk_loan_return_staff    FOREIGN KEY (return_staff_id)   REFERENCES staff_member(id)
);

-- ============================================================
-- TABLE: loan_renewal
-- Records each renewal event for a loan
-- ============================================================
CREATE TABLE loan_renewal (
    id              BIGINT    AUTO_INCREMENT PRIMARY KEY,
    loan_id         BIGINT    NOT NULL,
    renewed_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_due_date    TIMESTAMP NOT NULL,
    new_due_date    TIMESTAMP NOT NULL,
    renewed_by      VARCHAR(200),
    renewal_method  VARCHAR(20) CHECK (renewal_method IN ('ONLINE','PHONE','IN_PERSON','AUTO')),
    CONSTRAINT fk_renewal_loan FOREIGN KEY (loan_id) REFERENCES loan(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: hold
-- A reservation/request for a book that is currently unavailable
-- ============================================================
CREATE TABLE hold (
    id                BIGINT    AUTO_INCREMENT PRIMARY KEY,
    book_id           BIGINT    NOT NULL,
    member_id         BIGINT    NOT NULL,
    pickup_branch_id  BIGINT    NOT NULL,
    placed_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_date      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date       TIMESTAMP,
    notified_date     TIMESTAMP,
    fulfilled_date    TIMESTAMP,
    cancelled_date    TIMESTAMP,
    cancelled_by      VARCHAR(200),
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING','READY','FULFILLED','CANCELLED','EXPIRED')),
    queue_position    INT         NOT NULL DEFAULT 1,
    book_copy_id      BIGINT,
    notes             VARCHAR(500),
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hold_book          FOREIGN KEY (book_id)          REFERENCES book(id),
    CONSTRAINT fk_hold_member        FOREIGN KEY (member_id)        REFERENCES member(id),
    CONSTRAINT fk_hold_pickup_branch FOREIGN KEY (pickup_branch_id) REFERENCES library_branch(id),
    CONSTRAINT fk_hold_copy          FOREIGN KEY (book_copy_id)     REFERENCES book_copy(id)
);

-- ============================================================
-- TABLE: fine
-- Monetary fines associated with overdue or lost loans
-- ============================================================
CREATE TABLE fine (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    loan_id         BIGINT        NOT NULL,
    member_id       BIGINT        NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    amount_paid     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    reason          VARCHAR(500)  NOT NULL,
    reason_code     VARCHAR(50)   CHECK (reason_code IN ('OVERDUE','LOST_ITEM','DAMAGED_ITEM','PROCESSING_FEE')),
    issued_date     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid            BOOLEAN       NOT NULL DEFAULT FALSE,
    paid_date       TIMESTAMP,
    payment_method  VARCHAR(50)   CHECK (payment_method IN ('CASH','CARD','ONLINE','WAIVED')),
    waived          BOOLEAN       NOT NULL DEFAULT FALSE,
    waived_by       VARCHAR(200),
    waived_reason   VARCHAR(500),
    waive_reason    VARCHAR(500),
    processed_by    VARCHAR(200),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fine_loan_id  UNIQUE (loan_id),
    CONSTRAINT fk_fine_loan     FOREIGN KEY (loan_id)   REFERENCES loan(id),
    CONSTRAINT fk_fine_member   FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_fine_amount  CHECK (amount >= 0),
    CONSTRAINT chk_fine_paid_amount CHECK (amount_paid >= 0)
);

-- ============================================================
-- TABLE: fine_payment
-- Individual payment transactions applied to fines
-- ============================================================
CREATE TABLE fine_payment (
    id             BIGINT        AUTO_INCREMENT PRIMARY KEY,
    fine_id        BIGINT        NOT NULL,
    member_id      BIGINT        NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50)   NOT NULL,
    transaction_ref VARCHAR(100),
    paid_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_by   VARCHAR(200),
    notes          VARCHAR(500),
    CONSTRAINT fk_payment_fine   FOREIGN KEY (fine_id)   REFERENCES fine(id),
    CONSTRAINT fk_payment_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT chk_payment_amount CHECK (amount > 0)
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_loan_book_copy_id      ON loan(book_copy_id);
CREATE INDEX idx_loan_member_id         ON loan(member_id);
CREATE INDEX idx_loan_branch_id         ON loan(branch_id);
CREATE INDEX idx_loan_due_date          ON loan(due_date);
CREATE INDEX idx_loan_status            ON loan(status);
CREATE INDEX idx_loan_member_status     ON loan(member_id, status);
CREATE INDEX idx_loan_checkout_date     ON loan(checkout_date);
CREATE INDEX idx_loan_return_date       ON loan(return_date);
CREATE INDEX idx_loan_renewal_loan      ON loan_renewal(loan_id);
CREATE INDEX idx_hold_book_id           ON hold(book_id);
CREATE INDEX idx_hold_member_id         ON hold(member_id);
CREATE INDEX idx_hold_status            ON hold(status);
CREATE INDEX idx_hold_status_expiry     ON hold(status, expiry_date);
CREATE INDEX idx_hold_pickup_branch     ON hold(pickup_branch_id);
CREATE INDEX idx_hold_queue_position    ON hold(book_id, queue_position);
CREATE INDEX idx_fine_loan_id           ON fine(loan_id);
CREATE INDEX idx_fine_member_id         ON fine(member_id);
CREATE INDEX idx_fine_paid              ON fine(paid);
CREATE INDEX idx_fine_issued_date       ON fine(issued_date);
CREATE INDEX idx_fine_payment_fine      ON fine_payment(fine_id);
CREATE INDEX idx_fine_payment_member    ON fine_payment(member_id);
