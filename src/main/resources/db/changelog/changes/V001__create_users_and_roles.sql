-- V001: Core authentication tables
-- Creates app_user, role, user_role, and refresh_token tables
-- Supports JWT-based authentication with role-based access control

-- ============================================================
-- TABLE: app_user
-- Core user account table for all system users
-- ============================================================
CREATE TABLE app_user (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    username             VARCHAR(100)  NOT NULL,
    email                VARCHAR(255)  NOT NULL,
    password_hash        VARCHAR(255)  NOT NULL,
    first_name           VARCHAR(100),
    last_name            VARCHAR(100),
    enabled              BOOLEAN       NOT NULL DEFAULT TRUE,
    account_locked       BOOLEAN       NOT NULL DEFAULT FALSE,
    failed_login_attempts INT          NOT NULL DEFAULT 0,
    last_login           TIMESTAMP,
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_app_user_username UNIQUE (username),
    CONSTRAINT uq_app_user_email    UNIQUE (email)
);

-- ============================================================
-- TABLE: role
-- Application roles for RBAC
-- ============================================================
CREATE TABLE role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_role_name UNIQUE (name)
);

-- ============================================================
-- TABLE: user_role
-- Many-to-many join between app_user and role
-- ============================================================
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id)
);

-- ============================================================
-- TABLE: refresh_token
-- Stores JWT refresh tokens for session management
-- ============================================================
CREATE TABLE refresh_token (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user  FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: password_reset_token
-- Stores one-time tokens for password reset flow
-- ============================================================
CREATE TABLE password_reset_token (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_password_reset_token UNIQUE (token),
    CONSTRAINT fk_password_reset_user  FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: user_preference
-- Per-user application preferences (notification settings, UI prefs, etc.)
-- ============================================================
CREATE TABLE user_preference (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT       NOT NULL,
    preference_key       VARCHAR(100) NOT NULL,
    preference_value     VARCHAR(500),
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_pref_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_preference UNIQUE (user_id, preference_key)
);

-- ============================================================
-- Indexes on app_user for common lookups
-- ============================================================
CREATE INDEX idx_app_user_username   ON app_user(username);
CREATE INDEX idx_app_user_email      ON app_user(email);
CREATE INDEX idx_app_user_enabled    ON app_user(enabled);
CREATE INDEX idx_refresh_token_user  ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_expiry ON refresh_token(expires_at);

-- ============================================================
-- Seed: default roles
-- ============================================================
INSERT INTO role (name, description) VALUES
    ('ROLE_ADMIN',  'System administrator with full access to all features'),
    ('ROLE_STAFF',  'Library staff member with operational access'),
    ('ROLE_MEMBER', 'Registered library member with borrowing privileges');
