-- Очистка данных
TRUNCATE TABLE music_band CASCADE;
TRUNCATE TABLE person CASCADE;
TRUNCATE TABLE album CASCADE;
TRUNCATE TABLE coordinates CASCADE;
TRUNCATE TABLE location CASCADE;

-- Сбрасываем последовательности
ALTER SEQUENCE location_id_seq RESTART WITH 1;
ALTER SEQUENCE coordinates_id_seq RESTART WITH 1;
ALTER SEQUENCE album_id_seq RESTART WITH 1;
ALTER SEQUENCE person_id_seq RESTART WITH 1;
ALTER SEQUENCE music_band_id_seq RESTART WITH 1;
ALTER SEQUENCE best_band_award_id_seq RESTART WITH 1;

-- Вставляем тестовые локации
INSERT INTO location (x, y, z)
VALUES (10, 100, 1000),
       (20, 200, 2000),
       (30, 300, 3000),
       (40, 400, 4000),
       (50, 500, 5000);

-- Вставляем тестовые координаты
INSERT INTO coordinates (x, y)
VALUES (100, 50.5),
       (200, 60.5),
       (300, 70.5),
       (400, 80.5),
       (500, 90.5);

-- Вставляем тестовые альбомы
INSERT INTO album (name, tracks, sales)
VALUES ('Dark Side of the Moon', 10, 45000000),
       ('The Wall', 26, 30000000),
       ('Led Zeppelin IV', 8, 37000000),
       ('Back in Black', 10, 50000000),
       ('Thriller', 9, 66000000);

-- Вставляем тестовых людей (исправленные значения цветов согласно enum Color)
INSERT INTO person (name, eye_color, hair_color, location_id, weight, nationality)
VALUES ('Roger Waters', 'BLUE', 'BLACK', 1, 75.5, 'UK'),
       ('David Gilmour', 'GREEN', 'BROWN', 2, 80.0, 'UK'),
       ('Robert Plant', 'BROWN', 'BLACK', 3, 78.0, 'UK'),
       ('Angus Young', 'BLACK', 'BLACK', 4, 65.0, 'USA'),
       ('Michael Jackson', 'BROWN', 'BLACK', 5, 62.5, 'USA');

-- Вставляем тестовые музыкальные группы
INSERT INTO music_band (name, coordinates_id, genre, number_of_participants, singles_count, description, best_album_id,
                        albums_count, establishment_date, front_man_id)
VALUES ('Pink Floyd', 1, 'PROGRESSIVE_ROCK', 5, 27, 'Legendary progressive rock band from UK', 1, 15,
        '1965-01-01 00:00:00', 1),
       ('Led Zeppelin', 2, 'ROCK', 4, 18, 'Iconic rock band that defined hard rock', 3, 9, '1968-09-25 00:00:00', 3),
       ('AC/DC', 3, 'ROCK', 5, 25, 'Australian hard rock band known for high voltage performance', 4, 17,
        '1973-11-01 00:00:00', 4),
       ('Michael Jackson', 4, 'SOUL', 1, 65, 'King of Pop solo career', 5, 10, '1971-01-01 00:00:00', 5);

INSERT INTO best_band_award (band_id, genre, created_at)
VALUES (1, 'PROGRESSIVE_ROCK', '2024-01-15 10:00:00'), -- Pink Floyd - лучшая в прогрессивном роке
       (2, 'ROCK', '2024-02-20 11:30:00'),             -- Led Zeppelin - лучшая в роке
       (3, 'ROCK', '2023-12-10 09:15:00'),             -- AC/DC - лучшая в роке (другой год)
       (1, 'PROGRESSIVE_ROCK', '2023-06-05 14:20:00'), -- Pink Floyd - снова лучшая в прогрессивном роке
       (4, 'SOUL', '2024-03-01 16:45:00'); -- Michael Jackson - лучшая в соуле
