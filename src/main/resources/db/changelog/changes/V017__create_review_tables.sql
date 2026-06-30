-- V017: Book review and library event tables
-- Purpose: Member-submitted book reviews with ratings,
--          library event management, and event registration tracking.

-- ============================================================
-- TABLE: book_review
-- A star rating and optional written review submitted by a member.
-- One review per member per book; enforced by unique constraint.
-- ============================================================
CREATE TABLE IF NOT EXISTS book_review (
    id            BIGINT      AUTO_INCREMENT PRIMARY KEY,
    book_id       BIGINT      NOT NULL,
    member_id     BIGINT      NOT NULL,
    rating        INT         NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title         VARCHAR(300),
    content       TEXT,
    reviewed_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    helpful_count INT         NOT NULL DEFAULT 0,
    flagged       BOOLEAN     NOT NULL DEFAULT FALSE,
    approved      BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_book_review_member UNIQUE (book_id, member_id),
    CONSTRAINT fk_book_review_book   FOREIGN KEY (book_id)   REFERENCES book(id)   ON DELETE CASCADE,
    CONSTRAINT fk_book_review_member FOREIGN KEY (member_id) REFERENCES member(id)
);

-- ============================================================
-- TABLE: review_helpful_vote
-- Tracks which members marked a review as helpful (prevents duplicates).
-- ============================================================
CREATE TABLE IF NOT EXISTS review_helpful_vote (
    id         BIGINT    AUTO_INCREMENT PRIMARY KEY,
    review_id  BIGINT    NOT NULL,
    member_id  BIGINT    NOT NULL,
    voted_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_helpful_vote     UNIQUE (review_id, member_id),
    CONSTRAINT fk_vote_review      FOREIGN KEY (review_id)  REFERENCES book_review(id) ON DELETE CASCADE,
    CONSTRAINT fk_vote_member      FOREIGN KEY (member_id)  REFERENCES member(id)
);

-- ============================================================
-- TABLE: library_event
-- A library-hosted public event (author talks, workshops, exhibitions, etc.)
-- ============================================================
CREATE TABLE IF NOT EXISTS library_event (
    id                    BIGINT        AUTO_INCREMENT PRIMARY KEY,
    title                 VARCHAR(300)  NOT NULL,
    description           TEXT,
    event_date            TIMESTAMP     NOT NULL,
    end_date              TIMESTAMP,
    location              VARCHAR(500),
    branch_id             BIGINT,
    event_type            VARCHAR(50)
                                        CHECK (event_type IN ('AUTHOR_TALK', 'BOOK_FAIR', 'STORY_TIME',
                                                              'WORKSHOP', 'EXHIBITION', 'OTHER')),
    max_attendees         INT,
    registration_required BOOLEAN       DEFAULT FALSE,
    registration_deadline TIMESTAMP,
    cost                  DECIMAL(10,2) DEFAULT 0.00,
    cancelled             BOOLEAN       DEFAULT FALSE,
    cancel_reason         VARCHAR(500),
    image_url             VARCHAR(1000),
    external_url          VARCHAR(1000),
    created_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(200),
    CONSTRAINT fk_library_event_branch FOREIGN KEY (branch_id) REFERENCES library_branch(id)
);

-- ============================================================
-- TABLE: event_registration
-- Tracks member sign-ups and waitlist positions for library events.
-- One registration per member per event; enforced by unique constraint.
-- ============================================================
CREATE TABLE IF NOT EXISTS event_registration (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    event_id            BIGINT       NOT NULL,
    member_id           BIGINT       NOT NULL,
    registered_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attended            BOOLEAN      DEFAULT FALSE,
    waitlisted          BOOLEAN      DEFAULT FALSE,
    waitlist_position   INT,
    cancelled           BOOLEAN      DEFAULT FALSE,
    cancellation_reason VARCHAR(500),
    notes               VARCHAR(500),
    CONSTRAINT uq_event_registration UNIQUE (event_id, member_id),
    CONSTRAINT fk_event_reg_event    FOREIGN KEY (event_id)   REFERENCES library_event(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_reg_member   FOREIGN KEY (member_id)  REFERENCES member(id)
);

-- ============================================================
-- Indexes on book_review
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_book_review_book      ON book_review(book_id);
CREATE INDEX IF NOT EXISTS idx_book_review_member    ON book_review(member_id);
CREATE INDEX IF NOT EXISTS idx_book_review_rating    ON book_review(rating);
CREATE INDEX IF NOT EXISTS idx_book_review_approved  ON book_review(approved);
CREATE INDEX IF NOT EXISTS idx_book_review_flagged   ON book_review(flagged);
CREATE INDEX IF NOT EXISTS idx_book_review_date      ON book_review(reviewed_at);

-- ============================================================
-- Indexes on library_event
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_library_event_date    ON library_event(event_date);
CREATE INDEX IF NOT EXISTS idx_library_event_branch  ON library_event(branch_id);
CREATE INDEX IF NOT EXISTS idx_library_event_type    ON library_event(event_type);
CREATE INDEX IF NOT EXISTS idx_library_event_cancelled ON library_event(cancelled);

-- ============================================================
-- Indexes on event_registration
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_event_registration_event    ON event_registration(event_id);
CREATE INDEX IF NOT EXISTS idx_event_registration_member   ON event_registration(member_id);
CREATE INDEX IF NOT EXISTS idx_event_registration_waitlist ON event_registration(event_id, waitlisted);
