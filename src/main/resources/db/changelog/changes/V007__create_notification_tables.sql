-- V007: Notification tables
-- Creates notification and notification_template tables
-- Supports multi-channel member communications (email, SMS, push)

-- ============================================================
-- TABLE: notification_template
-- Reusable templates for system-generated notifications
-- ============================================================
CREATE TABLE notification_template (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    type             VARCHAR(30)  NOT NULL
                                  CHECK (type IN ('HOLD_READY','OVERDUE','WELCOME','FINE_ISSUED','MEMBERSHIP_EXPIRING',
                                                   'DUE_SOON','ITEM_RENEWED','ACCOUNT_LOCKED','PASSWORD_RESET',
                                                   'DIGITAL_LOAN_EXPIRING','CLUB_MEETING','CHALLENGE_COMPLETE')),
    subject_template VARCHAR(255),
    body_template    TEXT         NOT NULL,
    channel          VARCHAR(20)  NOT NULL
                                  CHECK (channel IN ('EMAIL','SMS','PUSH','IN_APP')),
    locale           VARCHAR(10)  NOT NULL DEFAULT 'en',
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_notification_template_name UNIQUE (name)
);

-- ============================================================
-- TABLE: notification
-- Individual notification records sent to members
-- ============================================================
CREATE TABLE notification (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT       NOT NULL,
    type            VARCHAR(30)  NOT NULL,
    subject         VARCHAR(255),
    message         TEXT         NOT NULL,
    channel         VARCHAR(20)  NOT NULL
                                 CHECK (channel IN ('EMAIL','SMS','PUSH','IN_APP')),
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMP,
    sent_at         TIMESTAMP,
    failed_at       TIMESTAMP,
    failure_reason  VARCHAR(500),
    reference_id    BIGINT,
    reference_type  VARCHAR(50),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: notification_batch
-- Groups related notifications sent as part of a scheduled job
-- (e.g. all overdue notices sent in a nightly run)
-- ============================================================
CREATE TABLE notification_batch (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    batch_type        VARCHAR(50)  NOT NULL,
    started_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at      TIMESTAMP,
    total_sent        INT          NOT NULL DEFAULT 0,
    total_failed      INT          NOT NULL DEFAULT 0,
    status            VARCHAR(20)  NOT NULL DEFAULT 'RUNNING'
                                   CHECK (status IN ('RUNNING','COMPLETED','FAILED')),
    notes             TEXT
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_notification_member_id    ON notification(member_id);
CREATE INDEX idx_notification_is_read      ON notification(is_read);
CREATE INDEX idx_notification_member_read  ON notification(member_id, is_read);
CREATE INDEX idx_notification_type         ON notification(type);
CREATE INDEX idx_notification_channel      ON notification(channel);
CREATE INDEX idx_notification_sent_at      ON notification(sent_at);
CREATE INDEX idx_notification_ref          ON notification(reference_type, reference_id);
CREATE INDEX idx_notif_template_type       ON notification_template(type);
CREATE INDEX idx_notif_template_channel    ON notification_template(channel);
CREATE INDEX idx_notif_template_active     ON notification_template(active);
