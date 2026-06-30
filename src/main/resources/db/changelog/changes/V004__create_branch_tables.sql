-- V004: Library branch tables
-- Creates library_branch, staff_member, and branch_hours tables
-- Represents the physical library network

-- ============================================================
-- TABLE: library_branch
-- A physical library location
-- ============================================================
CREATE TABLE library_branch (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    code          VARCHAR(20),
    address       VARCHAR(500) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100),
    zip           VARCHAR(20),
    country       VARCHAR(100) DEFAULT 'USA',
    phone         VARCHAR(30),
    email         VARCHAR(255),
    fax           VARCHAR(30),
    website       VARCHAR(500),
    manager_name  VARCHAR(200),
    manager_email VARCHAR(255),
    opening_hours VARCHAR(1000),
    parking_info  VARCHAR(500),
    accessibility_notes VARCHAR(1000),
    latitude      DECIMAL(10,7),
    longitude     DECIMAL(10,7),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_library_branch_name UNIQUE (name),
    CONSTRAINT uq_library_branch_code UNIQUE (code)
);

-- ============================================================
-- TABLE: staff_member
-- Library employee linked to a branch and user account
-- ============================================================
CREATE TABLE staff_member (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    branch_id   BIGINT       NOT NULL,
    user_id     BIGINT,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(30),
    role        VARCHAR(50)  NOT NULL
                             CHECK (role IN ('LIBRARIAN','ASSISTANT','MANAGER','DIRECTOR','TECH_SUPPORT','VOLUNTEER')),
    department  VARCHAR(100),
    employee_id VARCHAR(50),
    hire_date   DATE         NOT NULL,
    end_date    DATE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    notes       VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_branch FOREIGN KEY (branch_id) REFERENCES library_branch(id),
    CONSTRAINT fk_staff_user   FOREIGN KEY (user_id)   REFERENCES app_user(id)
);

-- ============================================================
-- TABLE: branch_hours
-- Regular operating hours for each branch by day of week
-- ============================================================
CREATE TABLE branch_hours (
    id           BIGINT      AUTO_INCREMENT PRIMARY KEY,
    branch_id    BIGINT      NOT NULL,
    day_of_week  VARCHAR(10) NOT NULL
                             CHECK (day_of_week IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    open_time    TIME,
    close_time   TIME,
    is_closed    BOOLEAN     NOT NULL DEFAULT FALSE,
    notes        VARCHAR(200),
    CONSTRAINT uq_branch_day     UNIQUE (branch_id, day_of_week),
    CONSTRAINT fk_hours_branch   FOREIGN KEY (branch_id) REFERENCES library_branch(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: branch_closure
-- Special/holiday closures that override regular branch_hours
-- ============================================================
CREATE TABLE branch_closure (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    branch_id   BIGINT,
    closure_date DATE        NOT NULL,
    reason      VARCHAR(300) NOT NULL,
    all_branches BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_closure_branch FOREIGN KEY (branch_id) REFERENCES library_branch(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: branch_service
-- Services offered at a specific branch (e.g. 3D printing, computer lab)
-- ============================================================
CREATE TABLE branch_service (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    branch_id   BIGINT       NOT NULL,
    service_name VARCHAR(200) NOT NULL,
    description TEXT,
    available   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_branch_service_branch FOREIGN KEY (branch_id) REFERENCES library_branch(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_library_branch_active      ON library_branch(active);
CREATE INDEX idx_library_branch_city        ON library_branch(city);
CREATE INDEX idx_staff_branch_id            ON staff_member(branch_id);
CREATE INDEX idx_staff_user_id              ON staff_member(user_id);
CREATE INDEX idx_staff_active               ON staff_member(active);
CREATE INDEX idx_staff_role                 ON staff_member(role);
CREATE INDEX idx_branch_hours_branch        ON branch_hours(branch_id);
CREATE INDEX idx_branch_closure_date        ON branch_closure(closure_date);
CREATE INDEX idx_branch_closure_branch      ON branch_closure(branch_id);
