CREATE TABLE album
(
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    tracks BIGINT       NOT NULL CHECK (tracks > 0),
    sales  INT          NOT NULL CHECK (sales > 0)
);

CREATE TABLE coordinates
(
    id BIGSERIAL PRIMARY KEY,
    x  INT  NOT NULL CHECK (x > -147),
    y  REAL NOT NULL
);

CREATE TABLE location
(
    id BIGSERIAL PRIMARY KEY,
    x  INT    NOT NULL,
    y  BIGINT NOT NULL,
    z  BIGINT NOT NULL
);

CREATE TABLE person
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    eye_color   VARCHAR(50),
    hair_color  VARCHAR(50),
    weight      REAL         NOT NULL CHECK (weight > 0),
    nationality VARCHAR(50),
    location_id BIGINT REFERENCES location (id)
);

CREATE TABLE music_band
(
    id                     BIGSERIAL PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL,
    coordinates_id         BIGINT       NOT NULL REFERENCES coordinates (id),
    creation_date          DATE         NOT NULL,
    genre                  VARCHAR(50)  NOT NULL,
    number_of_participants BIGINT       NOT NULL CHECK (number_of_participants > 0),
    singles_count          BIGINT       NOT NULL CHECK (singles_count > 0),
    description            TEXT         NOT NULL,
    best_album_id          BIGINT       NOT NULL REFERENCES album (id),
    albums_count           BIGINT       NOT NULL CHECK (albums_count > 0),
    establishment_date     TIMESTAMP    NOT NULL,
    front_man_id           BIGINT       NOT NULL REFERENCES person (id)
);
