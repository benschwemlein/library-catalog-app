-- V003: Library member tables
-- Creates the member table linking library patrons to user accounts
-- Tracks membership tier, fines, contact info, and borrowing preferences

-- ============================================================
-- TABLE: member
-- Library patron profile, linked to app_user account
-- ============================================================
CREATE TABLE member (
    id                           BIGINT         AUTO_INCREMENT PRIMARY KEY,
    user_id                      BIGINT         NOT NULL,
    membership_number            VARCHAR(50)    NOT NULL,
    membership_tier              VARCHAR(20)    NOT NULL DEFAULT 'STANDARD'
                                                CHECK (membership_tier IN ('STANDARD','PREMIUM','STUDENT')),
    join_date                    DATE           NOT NULL,
    expiry_date                  DATE           NOT NULL,
    fine_balance                 DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    active                       BOOLEAN        NOT NULL DEFAULT TRUE,
    phone                        VARCHAR(20),
    address                      VARCHAR(500),
    city                         VARCHAR(100),
    state                        VARCHAR(100),
    postal_code                  VARCHAR(20),
    country                      VARCHAR(100)   DEFAULT 'USA',
    date_of_birth                DATE,
    preferred_notification_channel VARCHAR(20)  CHECK (preferred_notification_channel IN ('EMAIL','SMS','PUSH','NONE')),
    preferred_language           VARCHAR(20)    DEFAULT 'en',
    email_notifications_enabled  BOOLEAN        NOT NULL DEFAULT TRUE,
    sms_notifications_enabled    BOOLEAN        NOT NULL DEFAULT FALSE,
    marketing_emails_enabled     BOOLEAN        NOT NULL DEFAULT TRUE,
    max_loans_override           INT,
    home_branch_id               BIGINT,
    notes                        TEXT,
    created_at                   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_member_membership_number UNIQUE (membership_number),
    CONSTRAINT uq_member_user_id           UNIQUE (user_id),
    CONSTRAINT fk_member_user              FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT chk_member_fine_balance     CHECK (fine_balance >= 0.00)
);

-- ============================================================
-- TABLE: member_card
-- Physical or virtual library cards associated with a member
-- ============================================================
CREATE TABLE member_card (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id   BIGINT       NOT NULL,
    card_number VARCHAR(50)  NOT NULL,
    issued_date DATE         NOT NULL,
    expiry_date DATE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    card_type   VARCHAR(20)  NOT NULL DEFAULT 'STANDARD'
                             CHECK (card_type IN ('STANDARD','DIGITAL','REPLACEMENT')),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_member_card_number   UNIQUE (card_number),
    CONSTRAINT fk_member_card_member   FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: membership_renewal
-- Historical record of membership renewals
-- ============================================================
CREATE TABLE membership_renewal (
    id              BIGINT        AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT        NOT NULL,
    renewal_date    DATE          NOT NULL,
    old_expiry_date DATE          NOT NULL,
    new_expiry_date DATE          NOT NULL,
    tier            VARCHAR(20)   NOT NULL,
    amount_paid     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_method  VARCHAR(50),
    processed_by    VARCHAR(200),
    notes           VARCHAR(500),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_renewal_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes for member lookups
-- ============================================================
CREATE INDEX idx_member_user_id            ON member(user_id);
CREATE INDEX idx_member_membership_number  ON member(membership_number);
CREATE INDEX idx_member_membership_tier    ON member(membership_tier);
CREATE INDEX idx_member_active             ON member(active);
CREATE INDEX idx_member_expiry_date        ON member(expiry_date);
CREATE INDEX idx_member_fine_balance       ON member(fine_balance);
CREATE INDEX idx_member_home_branch        ON member(home_branch_id);
CREATE INDEX idx_member_city               ON member(city);
CREATE INDEX idx_member_card_member        ON member_card(member_id);
CREATE INDEX idx_membership_renewal_member ON membership_renewal(member_id);
