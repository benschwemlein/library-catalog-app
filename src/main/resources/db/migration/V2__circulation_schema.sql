-- V2: Circulation tables (book_copy, loan, hold, fine, notification, audit_log)

CREATE TABLE book_copy (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id      BIGINT      NOT NULL REFERENCES book(id),
    branch_id    BIGINT      NOT NULL REFERENCES library_branch(id),
    barcode      VARCHAR(50) NOT NULL UNIQUE,
    condition    VARCHAR(20) NOT NULL,
    status       VARCHAR(20) NOT NULL,
    acquired_date DATE,
    deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at   TIMESTAMP,
    version      BIGINT  NOT NULL DEFAULT 0
);

CREATE TABLE loan (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_copy_id  BIGINT    NOT NULL REFERENCES book_copy(id),
    member_id     BIGINT    NOT NULL REFERENCES member(id),
    branch_id     BIGINT    NOT NULL REFERENCES library_branch(id),
    checkout_date TIMESTAMP NOT NULL,
    due_date      TIMESTAMP NOT NULL,
    return_date   TIMESTAMP,
    renewal_count INT       NOT NULL DEFAULT 0,
    status        VARCHAR(20) NOT NULL,
    version       BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE hold (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id          BIGINT    NOT NULL REFERENCES book(id),
    member_id        BIGINT    NOT NULL REFERENCES member(id),
    pickup_branch_id BIGINT    REFERENCES library_branch(id),
    request_date     TIMESTAMP NOT NULL,
    expiry_date      TIMESTAMP,
    status           VARCHAR(20) NOT NULL,
    notified_date    TIMESTAMP,
    version          BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE fine (
    id           BIGINT         AUTO_INCREMENT PRIMARY KEY,
    loan_id      BIGINT         NOT NULL UNIQUE REFERENCES loan(id),
    member_id    BIGINT         NOT NULL REFERENCES member(id),
    amount       DECIMAL(10, 2) NOT NULL,
    reason       VARCHAR(500),
    issued_date  TIMESTAMP      NOT NULL,
    paid_date    TIMESTAMP,
    waived       BOOLEAN        NOT NULL DEFAULT FALSE,
    waived_by    VARCHAR(200),
    waived_reason VARCHAR(500)
);

CREATE TABLE notification (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT NOT NULL REFERENCES member(id),
    type       VARCHAR(50)  NOT NULL,
    channel    VARCHAR(20)  NOT NULL,
    message    TEXT         NOT NULL,
    sent_at    TIMESTAMP,
    read_at    TIMESTAMP,
    created_at TIMESTAMP    NOT NULL
);

CREATE TABLE audit_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id   BIGINT,
    user_id     BIGINT,
    timestamp   TIMESTAMP    NOT NULL,
    old_value   TEXT,
    new_value   TEXT,
    ip_address  VARCHAR(50)
);
