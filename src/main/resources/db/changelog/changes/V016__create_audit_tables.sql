-- V016: Audit and system tables
-- Purpose: Audit logging for all significant library operations,
--          full-text search logging, and system configuration storage.

-- ============================================================
-- TABLE: audit_log
-- Immutable record of every significant state change in the system.
-- old_values / new_values are stored as JSON text for flexibility.
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    entity_type  VARCHAR(100) NOT NULL,
    entity_id    BIGINT,
    action       VARCHAR(50)  NOT NULL,
    performed_by VARCHAR(200),
    performed_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_values   TEXT,
    new_values   TEXT,
    ip_address   VARCHAR(50),
    session_id   VARCHAR(100),
    notes        TEXT,
    success      BOOLEAN      DEFAULT TRUE
);

-- ============================================================
-- TABLE: search_log
-- Records every search query issued through the system.
-- Supports usage analytics, autocomplete tuning, and relevance monitoring.
-- ============================================================
CREATE TABLE IF NOT EXISTS search_log (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    query            VARCHAR(1000),
    user_id          BIGINT,
    session_id       VARCHAR(100),
    results_count    INT           DEFAULT 0,
    search_type      VARCHAR(50),
    filters_applied  VARCHAR(1000),
    searched_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response_time_ms INT
);

-- ============================================================
-- TABLE: system_config
-- Key/value store for runtime-configurable application settings.
-- Staff with ADMIN role may update editable entries through the UI.
-- ============================================================
CREATE TABLE IF NOT EXISTS system_config (
    config_key    VARCHAR(100)  PRIMARY KEY,
    config_value  VARCHAR(1000) NOT NULL,
    description   VARCHAR(500),
    editable      BOOLEAN       DEFAULT TRUE,
    last_modified TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    modified_by   VARCHAR(200)
);

-- ============================================================
-- TABLE: api_access_log
-- Tracks inbound API requests for rate-limiting and abuse detection.
-- ============================================================
CREATE TABLE IF NOT EXISTS api_access_log (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    endpoint        VARCHAR(500) NOT NULL,
    http_method     VARCHAR(10)  NOT NULL,
    user_id         BIGINT,
    ip_address      VARCHAR(50),
    user_agent      VARCHAR(500),
    status_code     INT,
    response_time_ms INT,
    requested_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message   VARCHAR(500)
);

-- ============================================================
-- Indexes on audit_log
-- ============================================================

-- Primary lookup: find all audit events for a specific entity
CREATE INDEX IF NOT EXISTS idx_audit_log_entity        ON audit_log(entity_type, entity_id);

-- Chronological sweep for reporting windows
CREATE INDEX IF NOT EXISTS idx_audit_log_performed_at  ON audit_log(performed_at);

-- Find all actions taken by a specific staff member
CREATE INDEX IF NOT EXISTS idx_audit_log_performed_by  ON audit_log(performed_by);

-- Filter by action type (e.g. 'CHECKOUT', 'DELETE', 'UPDATE')
CREATE INDEX IF NOT EXISTS idx_audit_log_action        ON audit_log(action);

-- Session-scoped audit trail
CREATE INDEX IF NOT EXISTS idx_audit_log_session       ON audit_log(session_id);

-- Failed-only audit sweep
CREATE INDEX IF NOT EXISTS idx_audit_log_success       ON audit_log(success);

-- ============================================================
-- Indexes on search_log
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_search_log_searched_at  ON search_log(searched_at);
CREATE INDEX IF NOT EXISTS idx_search_log_user         ON search_log(user_id);
CREATE INDEX IF NOT EXISTS idx_search_log_query        ON search_log(query);
CREATE INDEX IF NOT EXISTS idx_search_log_search_type  ON search_log(search_type);

-- ============================================================
-- Indexes on api_access_log
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_api_access_log_at       ON api_access_log(requested_at);
CREATE INDEX IF NOT EXISTS idx_api_access_log_user     ON api_access_log(user_id);
CREATE INDEX IF NOT EXISTS idx_api_access_log_endpoint ON api_access_log(endpoint);
