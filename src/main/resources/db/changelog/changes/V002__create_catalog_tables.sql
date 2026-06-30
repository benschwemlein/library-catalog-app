-- V002: Book catalog tables
-- Creates author, publisher, genre, subject, book, and all join tables
-- Supports full library catalog management with metadata

-- ============================================================
-- TABLE: publisher
-- Book publishers
-- ============================================================
CREATE TABLE publisher (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(300) NOT NULL,
    address      VARCHAR(500),
    city         VARCHAR(100),
    state        VARCHAR(100),
    country      VARCHAR(100) DEFAULT 'USA',
    website      VARCHAR(500),
    email        VARCHAR(255),
    phone        VARCHAR(30),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_publisher_name UNIQUE (name)
);

-- ============================================================
-- TABLE: author
-- Book authors
-- ============================================================
CREATE TABLE author (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name   VARCHAR(100),
    last_name    VARCHAR(100) NOT NULL,
    birth_date   DATE,
    death_date   DATE,
    nationality  VARCHAR(100),
    biography    TEXT,
    website      VARCHAR(500),
    photo_url    VARCHAR(1000),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: genre
-- Book genres/categories
-- ============================================================
CREATE TABLE genre (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    parent_id   BIGINT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_genre_name UNIQUE (name),
    CONSTRAINT fk_genre_parent FOREIGN KEY (parent_id) REFERENCES genre(id)
);

-- ============================================================
-- TABLE: subject
-- Subject headings for catalog classification (Library of Congress style)
-- ============================================================
CREATE TABLE subject (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(300) NOT NULL,
    description VARCHAR(500),
    code        VARCHAR(50),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_subject_name UNIQUE (name)
);

-- ============================================================
-- TABLE: book
-- Core catalog record for a bibliographic title
-- ============================================================
CREATE TABLE book (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn                VARCHAR(20)  UNIQUE,
    isbn13              VARCHAR(20)  UNIQUE,
    title               VARCHAR(500) NOT NULL,
    subtitle            VARCHAR(500),
    publisher_id        BIGINT,
    publication_year    INT,
    publication_date    DATE,
    edition             VARCHAR(50),
    pages               INT,
    language            VARCHAR(50)  NOT NULL DEFAULT 'English',
    description         TEXT,
    table_of_contents   TEXT,
    cover_image_url     VARCHAR(1000),
    dewey_decimal       VARCHAR(50),
    lc_call_number      VARCHAR(100),
    audience_level      VARCHAR(30)  CHECK (audience_level IN ('CHILDREN','YOUNG_ADULT','ADULT','PROFESSIONAL')),
    format              VARCHAR(30)  CHECK (format IN ('HARDCOVER','PAPERBACK','LARGE_PRINT','BOARD_BOOK','GRAPHIC_NOVEL')),
    series_name         VARCHAR(300),
    series_number       INT,
    average_rating      DECIMAL(3,2) DEFAULT 0.00,
    rating_count        INT          NOT NULL DEFAULT 0,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_publisher FOREIGN KEY (publisher_id) REFERENCES publisher(id)
);

-- ============================================================
-- TABLE: book_author
-- Many-to-many between book and author, with role
-- ============================================================
CREATE TABLE book_author (
    book_id     BIGINT       NOT NULL,
    author_id   BIGINT       NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'AUTHOR'
                             CHECK (role IN ('AUTHOR','CO_AUTHOR','EDITOR','ILLUSTRATOR','TRANSLATOR','FOREWORD')),
    author_order INT         NOT NULL DEFAULT 1,
    PRIMARY KEY (book_id, author_id, role),
    CONSTRAINT fk_book_author_book   FOREIGN KEY (book_id)   REFERENCES book(id)   ON DELETE CASCADE,
    CONSTRAINT fk_book_author_author FOREIGN KEY (author_id) REFERENCES author(id)
);

-- ============================================================
-- TABLE: book_genre
-- Many-to-many between book and genre
-- ============================================================
CREATE TABLE book_genre (
    book_id  BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, genre_id),
    CONSTRAINT fk_book_genre_book  FOREIGN KEY (book_id)  REFERENCES book(id)  ON DELETE CASCADE,
    CONSTRAINT fk_book_genre_genre FOREIGN KEY (genre_id) REFERENCES genre(id)
);

-- ============================================================
-- TABLE: book_subject
-- Many-to-many between book and subject heading
-- ============================================================
CREATE TABLE book_subject (
    book_id    BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    PRIMARY KEY (book_id, subject_id),
    CONSTRAINT fk_book_subject_book    FOREIGN KEY (book_id)    REFERENCES book(id)    ON DELETE CASCADE,
    CONSTRAINT fk_book_subject_subject FOREIGN KEY (subject_id) REFERENCES subject(id)
);

-- ============================================================
-- TABLE: book_tag
-- Free-form tags for enhanced discoverability
-- ============================================================
CREATE TABLE book_tag (
    book_id BIGINT       NOT NULL,
    tag     VARCHAR(100) NOT NULL,
    PRIMARY KEY (book_id, tag),
    CONSTRAINT fk_book_tag_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes for catalog search performance
-- ============================================================
CREATE INDEX idx_book_title           ON book(title);
CREATE INDEX idx_book_isbn            ON book(isbn);
CREATE INDEX idx_book_isbn13          ON book(isbn13);
CREATE INDEX idx_book_publisher       ON book(publisher_id);
CREATE INDEX idx_book_pub_year        ON book(publication_year);
CREATE INDEX idx_book_active          ON book(active);
CREATE INDEX idx_book_audience        ON book(audience_level);
CREATE INDEX idx_author_last_name     ON author(last_name);
CREATE INDEX idx_author_first_name    ON author(first_name);
CREATE INDEX idx_book_author_author   ON book_author(author_id);
CREATE INDEX idx_book_genre_genre     ON book_genre(genre_id);
CREATE INDEX idx_book_subject_subject ON book_subject(subject_id);
CREATE INDEX idx_genre_parent         ON genre(parent_id);
