-- V3: Feature tables (search, reviews, challenges, book clubs, digital resources)

CREATE TABLE book_review (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id     BIGINT NOT NULL REFERENCES book(id),
    member_id   BIGINT NOT NULL REFERENCES member(id),
    rating      INT    NOT NULL,
    review_text TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP    NOT NULL,
    reviewed_at TIMESTAMP
);

CREATE TABLE search_index (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   BIGINT      NOT NULL,
    content     TEXT        NOT NULL,
    updated_at  TIMESTAMP   NOT NULL
);

CREATE INDEX idx_search_index_entity ON search_index(entity_type, entity_id);

CREATE TABLE search_log (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    query        VARCHAR(500) NOT NULL,
    entity_type  VARCHAR(50),
    result_count INT,
    member_id    BIGINT REFERENCES member(id),
    searched_at  TIMESTAMP NOT NULL
);

CREATE TABLE reading_challenge (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    start_date          DATE         NOT NULL,
    end_date            DATE         NOT NULL,
    target_books        INT          NOT NULL,
    target_genre_names  VARCHAR(500),
    badge               VARCHAR(200),
    active              BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE challenge_participation (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_id   BIGINT NOT NULL REFERENCES reading_challenge(id),
    member_id      BIGINT NOT NULL REFERENCES member(id),
    enrolled_date  DATE   NOT NULL,
    completed_date DATE,
    UNIQUE (challenge_id, member_id)
);

CREATE TABLE challenge_progress (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    participation_id BIGINT NOT NULL REFERENCES challenge_participation(id),
    book_id          BIGINT NOT NULL REFERENCES book(id),
    completed_date   DATE   NOT NULL
);

CREATE TABLE book_club (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE book_club_membership (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    club_id      BIGINT      NOT NULL REFERENCES book_club(id),
    member_id    BIGINT      NOT NULL REFERENCES member(id),
    role         VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at    TIMESTAMP   NOT NULL,
    UNIQUE (club_id, member_id)
);

CREATE TABLE reading_list (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT       NOT NULL REFERENCES member(id),
    name       VARCHAR(200) NOT NULL,
    visibility VARCHAR(20)  NOT NULL DEFAULT 'PRIVATE',
    created_at TIMESTAMP    NOT NULL
);

CREATE TABLE reading_list_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    reading_list_id BIGINT NOT NULL REFERENCES reading_list(id),
    book_id         BIGINT NOT NULL REFERENCES book(id),
    priority        VARCHAR(20),
    added_at        TIMESTAMP NOT NULL
);
