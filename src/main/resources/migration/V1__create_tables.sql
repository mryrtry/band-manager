-- Таблица users
CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50) UNIQUE NOT NULL CHECK (LENGTH(username) BETWEEN 3 AND 50),
    password   VARCHAR(100)       NOT NULL CHECK (LENGTH(password) >= 6),
    is_active  BOOLEAN            NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE    DEFAULT CURRENT_TIMESTAMP
);

-- Таблица user_roles
CREATE TABLE user_roles
(
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role)
);

-- Таблица location
CREATE TABLE location
(
    id         BIGSERIAL PRIMARY KEY,
    x          INTEGER,
    y          BIGINT NOT NULL,
    z          BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблица coordinates
CREATE TABLE coordinates
(
    id         BIGSERIAL PRIMARY KEY,
    x          INTEGER NOT NULL CHECK (x > -147),
    y          REAL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблица album
CREATE TABLE album
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL CHECK (name <> ''),
    tracks     BIGINT       NOT NULL CHECK (tracks > 0),
    sales      INTEGER      NOT NULL CHECK (sales > 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Таблица person
CREATE TABLE person
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL CHECK (name <> ''),
    eye_color   VARCHAR(50)  NOT NULL CHECK (eye_color IN ('BLACK', 'ORANGE', 'BROWN', 'GREEN', 'BLUE')),
    hair_color  VARCHAR(50)  NOT NULL CHECK (hair_color IN ('BLACK', 'ORANGE', 'BROWN', 'GREEN', 'BLUE')),
    location_id BIGINT       NOT NULL,
    weight      REAL         NOT NULL CHECK (weight > 0),
    nationality VARCHAR(50)  NOT NULL CHECK (nationality IN ('FRANCE', 'INDIA', 'THAILAND', 'USA', 'UK')),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_person_location
        FOREIGN KEY (location_id) REFERENCES location (id)
            ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Таблица music_band
CREATE TABLE music_band
(
    id                     SERIAL PRIMARY KEY,
    name                   VARCHAR(255)             NOT NULL CHECK (name <> ''),
    coordinates_id         BIGINT                   NOT NULL,
    genre                  VARCHAR(50)              NOT NULL CHECK (genre IN
                                                                    ('PROGRESSIVE_ROCK', 'SOUL', 'ROCK', 'POST_ROCK',
                                                                     'PUNK_ROCK', 'POST_PUNK')),
    number_of_participants BIGINT                   NOT NULL CHECK (number_of_participants > 0),
    singles_count          BIGINT                   NOT NULL CHECK (singles_count > 0),
    description            TEXT                     NOT NULL CHECK (description <> ''),
    best_album_id          BIGINT                   NOT NULL,
    albums_count           BIGINT                   NOT NULL CHECK (albums_count > 0),
    establishment_date     TIMESTAMP WITH TIME ZONE NOT NULL,
    front_man_id           BIGINT                   NOT NULL,
    creation_date          TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_music_band_coordinates
        FOREIGN KEY (coordinates_id) REFERENCES coordinates (id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_music_band_best_album
        FOREIGN KEY (best_album_id) REFERENCES album (id)
            ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_music_band_front_man
        FOREIGN KEY (front_man_id) REFERENCES person (id)
            ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Таблица best_band_award
CREATE TABLE best_band_award
(
    id         BIGSERIAL PRIMARY KEY,
    band_id    INTEGER                  NOT NULL,
    genre      VARCHAR(50)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_best_band_award_band
        FOREIGN KEY (band_id) REFERENCES music_band (id)
            ON DELETE CASCADE ON UPDATE CASCADE
);

-- Индексы для улучшения производительности
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_active ON users (is_active);
CREATE INDEX idx_user_roles_user ON user_roles (user_id);
CREATE INDEX idx_user_roles_role ON user_roles (role);

CREATE INDEX idx_best_band_award_genre ON best_band_award (genre);
CREATE INDEX idx_best_band_award_band_id ON best_band_award (band_id);
CREATE INDEX idx_best_band_award_created_at ON best_band_award (created_at DESC);
CREATE INDEX idx_best_band_award_genre_created ON best_band_award (genre, created_at DESC);
CREATE INDEX idx_best_band_award_band_genre ON best_band_award (band_id, genre);

CREATE INDEX idx_location_xyz ON location (x, y, z);
CREATE INDEX idx_coordinates_xy ON coordinates (x, y);
CREATE INDEX idx_album_name ON album (name);
CREATE INDEX idx_album_sales ON album (sales);
CREATE INDEX idx_person_name ON person (name);
CREATE INDEX idx_person_nationality ON person (nationality);
CREATE INDEX idx_person_location ON person (location_id);
CREATE INDEX idx_person_eye_color ON person (eye_color);
CREATE INDEX idx_person_hair_color ON person (hair_color);

CREATE INDEX idx_music_band_name ON music_band (name);
CREATE INDEX idx_music_band_genre ON music_band (genre);
CREATE INDEX idx_music_band_establishment_date ON music_band (establishment_date);
CREATE INDEX idx_music_band_creation_date ON music_band (creation_date);
CREATE INDEX idx_music_band_coordinates ON music_band (coordinates_id);
CREATE INDEX idx_music_band_best_album ON music_band (best_album_id);
CREATE INDEX idx_music_band_front_man ON music_band (front_man_id);
CREATE INDEX idx_music_band_participants ON music_band (number_of_participants);
CREATE INDEX idx_music_band_albums_count ON music_band (albums_count);

-- Составные индексы для часто используемых запросов
CREATE INDEX idx_music_band_genre_participants ON music_band (genre, number_of_participants);
CREATE INDEX idx_music_band_date_genre ON music_band (establishment_date, genre);
CREATE INDEX idx_music_band_genre_establishment ON music_band (genre, establishment_date);