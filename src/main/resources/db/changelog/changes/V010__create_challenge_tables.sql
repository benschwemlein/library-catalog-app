-- V010: Reading challenge tables
-- Creates reading_challenge, challenge_participation, and challenge_progress
-- Supports gamified reading programs with badges and goals

-- ============================================================
-- TABLE: reading_challenge
-- A library-hosted reading challenge with goals and rewards
-- ============================================================
CREATE TABLE reading_challenge (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200)  NOT NULL,
    description         TEXT,
    start_date          DATE          NOT NULL,
    end_date            DATE          NOT NULL,
    target_books        INT           NOT NULL DEFAULT 1,
    target_pages        INT,
    target_genre_names  VARCHAR(1000),
    target_categories   VARCHAR(1000),
    badge_name          VARCHAR(100),
    badge_image_url     VARCHAR(1000),
    rules_text          TEXT,
    max_participants    INT,
    created_by_staff    VARCHAR(200),
    branch_id           BIGINT,
    age_group           VARCHAR(30)   CHECK (age_group IN ('CHILDREN','TEEN','ADULT','ALL_AGES')),
    difficulty          VARCHAR(20)   CHECK (difficulty IN ('EASY','MEDIUM','HARD','EXPERT')),
    active              BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_challenge_branch FOREIGN KEY (branch_id) REFERENCES library_branch(id)
);

-- ============================================================
-- TABLE: challenge_participation
-- Tracks member enrollment in a reading challenge
-- ============================================================
CREATE TABLE challenge_participation (
    id               BIGINT    AUTO_INCREMENT PRIMARY KEY,
    challenge_id     BIGINT    NOT NULL,
    member_id        BIGINT    NOT NULL,
    enrolled_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at     TIMESTAMP,
    completed        BOOLEAN   NOT NULL DEFAULT FALSE,
    badge_awarded    BOOLEAN   NOT NULL DEFAULT FALSE,
    badge_awarded_at TIMESTAMP,
    books_read       INT       NOT NULL DEFAULT 0,
    pages_read       INT       NOT NULL DEFAULT 0,
    notes            VARCHAR(500),
    CONSTRAINT uq_challenge_member     UNIQUE (challenge_id, member_id),
    CONSTRAINT fk_participation_challenge FOREIGN KEY (challenge_id) REFERENCES reading_challenge(id),
    CONSTRAINT fk_participation_member    FOREIGN KEY (member_id)    REFERENCES member(id)
);

-- ============================================================
-- TABLE: challenge_progress
-- Individual book entries logged toward a challenge
-- ============================================================
CREATE TABLE challenge_progress (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    participation_id BIGINT       NOT NULL,
    book_id          BIGINT,
    loan_id          BIGINT,
    book_title       VARCHAR(500),
    pages_counted    INT,
    recorded_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes            VARCHAR(500),
    verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_progress_participation FOREIGN KEY (participation_id) REFERENCES challenge_participation(id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_book          FOREIGN KEY (book_id)          REFERENCES book(id),
    CONSTRAINT fk_progress_loan          FOREIGN KEY (loan_id)          REFERENCES loan(id)
);

-- ============================================================
-- TABLE: member_badge
-- Badges awarded to members for challenges and achievements
-- ============================================================
CREATE TABLE member_badge (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT       NOT NULL,
    badge_name     VARCHAR(100) NOT NULL,
    badge_image_url VARCHAR(1000),
    description    VARCHAR(500),
    awarded_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source_type    VARCHAR(50)  CHECK (source_type IN ('CHALLENGE','ACHIEVEMENT','STAFF_AWARD','MILESTONE')),
    source_id      BIGINT,
    CONSTRAINT fk_member_badge_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: reading_goal
-- Personal reading goals set by individual members
-- ============================================================
CREATE TABLE reading_goal (
    id             BIGINT    AUTO_INCREMENT PRIMARY KEY,
    member_id      BIGINT    NOT NULL,
    year           INT       NOT NULL,
    target_books   INT       NOT NULL DEFAULT 12,
    books_read     INT       NOT NULL DEFAULT 0,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_reading_goal_member_year UNIQUE (member_id, year),
    CONSTRAINT fk_reading_goal_member      FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_challenge_active              ON reading_challenge(active);
CREATE INDEX idx_challenge_dates               ON reading_challenge(start_date, end_date);
CREATE INDEX idx_challenge_branch              ON reading_challenge(branch_id);
CREATE INDEX idx_participation_challenge       ON challenge_participation(challenge_id);
CREATE INDEX idx_participation_member          ON challenge_participation(member_id);
CREATE INDEX idx_participation_completed       ON challenge_participation(challenge_id, completed);
CREATE INDEX idx_progress_participation        ON challenge_progress(participation_id);
CREATE INDEX idx_progress_book_id              ON challenge_progress(book_id);
CREATE INDEX idx_member_badge_member           ON member_badge(member_id);
CREATE INDEX idx_reading_goal_member_year      ON reading_goal(member_id, year);
