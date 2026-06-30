-- V009: Community engagement tables
-- Creates book_club, book_club_membership, book_club_meeting,
-- and book_club_discussion tables
-- Supports the library's community programs and reading groups

-- ============================================================
-- TABLE: book_club
-- A library-hosted reading group
-- ============================================================
CREATE TABLE book_club (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(200) NOT NULL,
    description          TEXT,
    branch_id            BIGINT,
    facilitator_staff_id BIGINT,
    max_members          INT          NOT NULL DEFAULT 20,
    meeting_schedule     VARCHAR(500),
    meeting_location     VARCHAR(500),
    genre_focus          VARCHAR(200),
    age_group            VARCHAR(50)  CHECK (age_group IN ('CHILDREN','TEEN','ADULT','SENIOR','ALL_AGES')),
    current_book_id      BIGINT,
    cover_image_url      VARCHAR(1000),
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                                      CHECK (status IN ('ACTIVE','INACTIVE','DISBANDED','FORMING')),
    is_public            BOOLEAN      NOT NULL DEFAULT TRUE,
    requires_approval    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_club_branch      FOREIGN KEY (branch_id)            REFERENCES library_branch(id),
    CONSTRAINT fk_book_club_staff       FOREIGN KEY (facilitator_staff_id) REFERENCES staff_member(id),
    CONSTRAINT fk_book_club_current_book FOREIGN KEY (current_book_id)     REFERENCES book(id)
);

-- ============================================================
-- TABLE: book_club_membership
-- Tracks which members belong to which clubs
-- ============================================================
CREATE TABLE book_club_membership (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    club_id     BIGINT      NOT NULL,
    member_id   BIGINT      NOT NULL,
    role        VARCHAR(20) NOT NULL DEFAULT 'MEMBER'
                            CHECK (role IN ('MEMBER','MODERATOR','LEADER')),
    joined_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at     TIMESTAMP,
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    approved_by VARCHAR(200),
    notes       VARCHAR(500),
    CONSTRAINT uq_club_member        UNIQUE (club_id, member_id),
    CONSTRAINT fk_bcm_club           FOREIGN KEY (club_id)   REFERENCES book_club(id) ON DELETE CASCADE,
    CONSTRAINT fk_bcm_member         FOREIGN KEY (member_id) REFERENCES member(id)
);

-- ============================================================
-- TABLE: book_club_meeting
-- A scheduled or past meeting of a book club
-- ============================================================
CREATE TABLE book_club_meeting (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    club_id       BIGINT       NOT NULL,
    title         VARCHAR(300) NOT NULL,
    description   TEXT,
    meeting_date  TIMESTAMP    NOT NULL,
    location      VARCHAR(500),
    virtual_link  VARCHAR(1000),
    book_id       BIGINT,
    agenda        TEXT,
    notes         TEXT,
    attendee_count INT         NOT NULL DEFAULT 0,
    cancelled     BOOLEAN      NOT NULL DEFAULT FALSE,
    cancel_reason VARCHAR(300),
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meeting_club FOREIGN KEY (club_id) REFERENCES book_club(id) ON DELETE CASCADE,
    CONSTRAINT fk_meeting_book FOREIGN KEY (book_id) REFERENCES book(id)
);

-- ============================================================
-- TABLE: book_club_meeting_attendance
-- Records which members attended a meeting
-- ============================================================
CREATE TABLE book_club_meeting_attendance (
    id          BIGINT    AUTO_INCREMENT PRIMARY KEY,
    meeting_id  BIGINT    NOT NULL,
    member_id   BIGINT    NOT NULL,
    rsvp_status VARCHAR(20) CHECK (rsvp_status IN ('ATTENDING','MAYBE','DECLINED')),
    attended    BOOLEAN   NOT NULL DEFAULT FALSE,
    notes       VARCHAR(300),
    CONSTRAINT uq_meeting_attendance  UNIQUE (meeting_id, member_id),
    CONSTRAINT fk_attendance_meeting  FOREIGN KEY (meeting_id) REFERENCES book_club_meeting(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_member   FOREIGN KEY (member_id)  REFERENCES member(id)
);

-- ============================================================
-- TABLE: book_club_discussion
-- Threaded discussion posts within a club or meeting
-- ============================================================
CREATE TABLE book_club_discussion (
    id                    BIGINT       AUTO_INCREMENT PRIMARY KEY,
    club_id               BIGINT       NOT NULL,
    meeting_id            BIGINT,
    posted_by_member_id   BIGINT       NOT NULL,
    parent_discussion_id  BIGINT,
    title                 VARCHAR(300),
    content               TEXT         NOT NULL,
    posted_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at             TIMESTAMP,
    pinned                BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    helpful_count         INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_discussion_club    FOREIGN KEY (club_id)              REFERENCES book_club(id)         ON DELETE CASCADE,
    CONSTRAINT fk_discussion_meeting FOREIGN KEY (meeting_id)           REFERENCES book_club_meeting(id),
    CONSTRAINT fk_discussion_member  FOREIGN KEY (posted_by_member_id)  REFERENCES member(id),
    CONSTRAINT fk_discussion_parent  FOREIGN KEY (parent_discussion_id) REFERENCES book_club_discussion(id)
);

-- ============================================================
-- TABLE: book_club_book_history
-- Books previously read by a club
-- ============================================================
CREATE TABLE book_club_book_history (
    id          BIGINT    AUTO_INCREMENT PRIMARY KEY,
    club_id     BIGINT    NOT NULL,
    book_id     BIGINT    NOT NULL,
    started_at  DATE,
    finished_at DATE,
    selected_by VARCHAR(200),
    rating      INT       CHECK (rating BETWEEN 1 AND 5),
    notes       VARCHAR(500),
    CONSTRAINT uq_club_book_history UNIQUE (club_id, book_id),
    CONSTRAINT fk_history_club FOREIGN KEY (club_id) REFERENCES book_club(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_book FOREIGN KEY (book_id) REFERENCES book(id)
);

-- ============================================================
-- Indexes
-- ============================================================
CREATE INDEX idx_book_club_branch          ON book_club(branch_id);
CREATE INDEX idx_book_club_status          ON book_club(status);
CREATE INDEX idx_book_club_current_book    ON book_club(current_book_id);
CREATE INDEX idx_bcm_club_id              ON book_club_membership(club_id);
CREATE INDEX idx_bcm_member_id            ON book_club_membership(member_id);
CREATE INDEX idx_bcm_active               ON book_club_membership(club_id, active);
CREATE INDEX idx_meeting_club_id          ON book_club_meeting(club_id);
CREATE INDEX idx_meeting_date             ON book_club_meeting(meeting_date);
CREATE INDEX idx_discussion_club_id       ON book_club_discussion(club_id);
CREATE INDEX idx_discussion_meeting_id    ON book_club_discussion(meeting_id);
CREATE INDEX idx_discussion_parent_id     ON book_club_discussion(parent_discussion_id);
CREATE INDEX idx_discussion_member_id     ON book_club_discussion(posted_by_member_id);
