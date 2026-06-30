-- V1: Core entity tables (library_branch, member, book, author, publisher, genre)

CREATE TABLE library_branch (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(200) NOT NULL,
    address        VARCHAR(500),
    city           VARCHAR(100),
    phone          VARCHAR(50),
    email          VARCHAR(255),
    opening_hours  VARCHAR(500),
    active         BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE publisher (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(300) NOT NULL,
    country VARCHAR(100),
    website VARCHAR(500)
);

CREATE TABLE author (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    biography  TEXT,
    birth_date DATE
);

CREATE TABLE genre (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE book_subject (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE book (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn             VARCHAR(20)  NOT NULL UNIQUE,
    title            VARCHAR(500) NOT NULL,
    subtitle         VARCHAR(500),
    description      TEXT,
    publication_year INT,
    page_count       INT,
    language         VARCHAR(50),
    cover_image_url  VARCHAR(1000),
    publisher_id     BIGINT REFERENCES publisher(id),
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL,
    deleted          BOOLEAN   NOT NULL DEFAULT FALSE,
    deleted_at       TIMESTAMP,
    version          BIGINT    NOT NULL DEFAULT 0
);

CREATE TABLE book_author (
    book_id   BIGINT NOT NULL REFERENCES book(id),
    author_id BIGINT NOT NULL REFERENCES author(id),
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE book_genre (
    book_id  BIGINT NOT NULL REFERENCES book(id),
    genre_id BIGINT NOT NULL REFERENCES genre(id),
    PRIMARY KEY (book_id, genre_id)
);

CREATE TABLE book_book_subject (
    book_id    BIGINT NOT NULL REFERENCES book(id),
    subject_id BIGINT NOT NULL REFERENCES book_subject(id),
    PRIMARY KEY (book_id, subject_id)
);

CREATE TABLE member (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    membership_number VARCHAR(50)    NOT NULL UNIQUE,
    membership_tier   VARCHAR(20)    NOT NULL,
    join_date         DATE           NOT NULL,
    expiry_date       DATE           NOT NULL,
    fine_balance      DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    active            BOOLEAN        NOT NULL DEFAULT TRUE,
    deleted           BOOLEAN        NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMP,
    version           BIGINT         NOT NULL DEFAULT 0
);

CREATE TABLE staff_member (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    role       VARCHAR(50)  NOT NULL,
    branch_id  BIGINT REFERENCES library_branch(id),
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    start_date DATE
);
