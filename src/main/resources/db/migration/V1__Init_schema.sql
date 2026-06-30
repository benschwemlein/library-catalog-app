CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL -- Assuming your Role enum is stored as a string
);

CREATE TABLE IF NOT EXISTS catalog_id_type (
    name VARCHAR(255) NOT NULL,
    max_length INT NOT NULL,
    format_regex VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS catalog_id (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    CONSTRAINT fk_catalog_id_catalog_id_type
        FOREIGN KEY (type)
        REFERENCES catalog_id_type(name)
  
);

CREATE TABLE IF NOT EXISTS catalog_item  (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_by BIGINT,  -- Assuming this is a foreign key to a `user` table.
    created_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS catalog_item_catalog_id (
    catalog_item_id BIGINT NOT NULL,
    catalog_id_id BIGINT NOT NULL,
    PRIMARY KEY (catalog_item_id, catalog_id_id),
    FOREIGN KEY (catalog_item_id) REFERENCES catalog_item(id),
    FOREIGN KEY (catalog_id_id) REFERENCES catalog_id(id)
);

CREATE TABLE IF NOT EXISTS checkout (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    checked_out BOOLEAN NOT NULL,
    checkout_date_time TIMESTAMP,
    checkin_date_time TIMESTAMP,
    checkedout_by BIGINT NOT NULL,
    CONSTRAINT fk_item
        FOREIGN KEY (item_id)
        REFERENCES catalog_item(id),
    CONSTRAINT fk_user
        FOREIGN KEY (checkedout_by)
        REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL,
    revoked BOOLEAN NOT NULL,
    expired BOOLEAN NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id)
);


INSERT INTO catalog_id_type (name, max_length, format_regex)
VALUES('ISBN', 13, '^(?=(?:\D*\d){10}(?:(?:\D*\d){3})?$)[\d-]+$');

INSERT INTO catalog_id_type (name, max_length, format_regex)
VALUES('LCCN', 12, '^[A-Za-z\s]{2}\d{4}\d{6}$');




