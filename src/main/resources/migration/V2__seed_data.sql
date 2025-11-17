-- 1. Очистка таблиц
TRUNCATE TABLE import_operations CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE music_band CASCADE;
TRUNCATE TABLE person CASCADE;
TRUNCATE TABLE album CASCADE;
TRUNCATE TABLE coordinates CASCADE;
TRUNCATE TABLE location CASCADE;
TRUNCATE TABLE best_band_award CASCADE;

-- 2. Сброс счётчиков ID
ALTER SEQUENCE import_operations_id_seq RESTART WITH 1;
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE location_id_seq RESTART WITH 1;
ALTER SEQUENCE coordinates_id_seq RESTART WITH 1;
ALTER SEQUENCE album_id_seq RESTART WITH 1;
ALTER SEQUENCE person_id_seq RESTART WITH 1;
ALTER SEQUENCE music_band_id_seq RESTART WITH 1;
ALTER SEQUENCE best_band_award_id_seq RESTART WITH 1;

-- 3. Заполнение таблиц без внешних ключей (зависимостей)

-- Заполнение location
INSERT INTO location (x, y, z, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    (random() * 1000 - 500)::integer,
    (random() * 2000 - 1000)::bigint,
    (random() * 5000)::bigint,
    'system',
    base_created,
    'system',
    base_created + (random() * 30)::int * INTERVAL '1 day' -- last_modified >= created
FROM (
         SELECT
                     CURRENT_TIMESTAMP - (random() * 300)::integer * INTERVAL '1 day' AS base_created
         FROM generate_series(1, 200)
     ) s;

-- Заполнение coordinates
INSERT INTO coordinates (x, y, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    (random() * 180 - 90)::integer,
    (random() * 360 - 180)::real,
    'system',
    base_created,
    'system',
    base_created + (random() * 30)::int * INTERVAL '1 day'
FROM (
         SELECT
                     CURRENT_TIMESTAMP - (random() * 300)::integer * INTERVAL '1 day' AS base_created
         FROM generate_series(1, 200)
     ) s;

-- Заполнение album
INSERT INTO album (name, tracks, sales, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    CASE (random() * 9)::int
        WHEN 0 THEN 'Echoes of ' || ('{Time,Destiny,Silence,Memory,Freedom,Hope,Dreams,Reality,Eternity}'::text[])[(random() * 8)::int + 1]
        WHEN 1 THEN 'The ' || ('{Last,First,Final,Lost,Secret,Ancient,Modern,Digital}'::text[])[(random() * 7)::int + 1] || ' ' || ('{Journey,Chapter,Whisper,Scream,Revolution,Evolution}'::text[])[(random() * 5)::int + 1]
        WHEN 2 THEN ('{Midnight,Sunrise,Twilight,Dawn,Dusk,Noon}'::text[])[(random() * 5)::int + 1] || ' ' || ('{Sessions,Chronicles,Stories,Memories}'::text[])[(random() * 3)::int + 1]
        WHEN 3 THEN ('{Electric,Acoustic,Digital,Analog,Neon,Urban}'::text[])[(random() * 5)::int + 1] || ' ' || ('{Dreams,Nights,Days,Waves,Currents}'::text[])[(random() * 4)::int + 1]
        WHEN 4 THEN 'Volume ' || (random() * 9 + 1)::int
        WHEN 5 THEN ('{Northern,Southern,Eastern,Western}'::text[])[(random() * 3)::int + 1] || ' ' || ('{Lights,Winds,Skies,Seas}'::text[])[(random() * 3)::int + 1]
        WHEN 6 THEN 'The ' || ('{Red,Blue,Green,Black,White,Golden,Silver}'::text[])[(random() * 6)::int + 1] || ' ' || ('{Album,Record,Collection,Anthology}'::text[])[(random() * 3)::int + 1]
        WHEN 7 THEN ('{Chronicles,Legends,Myths,Fables}'::text[])[(random() * 3)::int + 1] || ' of ' || ('{Tomorrow,Yesterday,Today,Forever}'::text[])[(random() * 3)::int + 1]
        ELSE 'Unknown Sessions ' || (random() * 1000)::int
        END,
    (random() * 20 + 1)::bigint,
    (random() * 1000000 + 1000)::integer,
    'system',
    base_created,
    'system',
    base_created + (random() * 30)::int * INTERVAL '1 day'
FROM (
         SELECT
                     CURRENT_TIMESTAMP - (random() * 300)::integer * INTERVAL '1 day' AS base_created
         FROM generate_series(1, 200)
     ) s;

-- Заполнение person
INSERT INTO person (name, eye_color, hair_color, location_id, weight, nationality, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    CASE (random() * 4)::int
        WHEN 0 THEN ('{John,Michael,David,Robert,James,William,Richard,Thomas,Christopher,Daniel}'::text[])[(random() * 9)::int + 1] || ' ' || ('{Smith,Johnson,Williams,Brown,Jones,Miller,Davis,Garcia,Wilson,Anderson}'::text[])[(random() * 9)::int + 1]
        WHEN 1 THEN ('{Emma,Olivia,Ava,Isabella,Sophia,Charlotte,Mia,Amelia,Harper,Evelyn}'::text[])[(random() * 9)::int + 1] || ' ' || ('{Taylor,Moore,Clark,Allen,Young,King,Wright,Scott,Green,Hall}'::text[])[(random() * 9)::int + 1]
        WHEN 2 THEN ('{Alex,Jordan,Casey,Riley,Avery,Cameron,Charlie,Finley,Harley,Skyler}'::text[])[(random() * 9)::int + 1] || ' ' || ('{Lee,Walker,Hill,Adams,Nelson,Baker,Carter,Mitchell,Perez,Roberts}'::text[])[(random() * 9)::int + 1]
        ELSE ('{Max,Luca,Noah,Ethan,Leo,Jack,Oscar,Archie,Henry,Jacob}'::text[])[(random() * 9)::int + 1] || ' ' || ('{Harris,Martin,Thompson,White,Lopez,Clark,Rodriguez,Lewis,Lee,Walker}'::text[])[(random() * 9)::int + 1]
        END,
    ('{BLACK,ORANGE,BROWN,GREEN,BLUE}'::text[])[(random() * 4)::int + 1],
    ('{BLACK,ORANGE,BROWN,GREEN,BLUE}'::text[])[(random() * 4)::int + 1],
    (random() * 199 + 1)::int, -- location_id
    (random() * 50 + 50)::real,
    ('{FRANCE,INDIA,THAILAND,USA,UK}'::text[])[(random() * 4)::int + 1],
    'system',
    base_created,
    'system',
    base_created + (random() * 30)::int * INTERVAL '1 day'
FROM (
         SELECT
                     CURRENT_TIMESTAMP - (random() * 300)::integer * INTERVAL '1 day' AS base_created
         FROM generate_series(1, 200)
     ) s;

-- 4. Заполнение таблиц с внешними ключами

-- Заполнение music_band (требует person, album, coordinates)
INSERT INTO music_band (
    name, coordinates_id, genre, number_of_participants, singles_count, description,
    best_album_id, albums_count, establishment_date, front_man_id,
    created_by, created_date, last_modified_by, last_modified_date
)
SELECT
    -- Генерация уникальных имен групп
    CASE (random() * 7)::int
        WHEN 0 THEN 'The ' || ('{Echoes,Voices,Shadows,Lights,Currents,Waves,Elements,Forces}'::text[])[(random() * 7)::int + 1] || ' ' || (row_number() over ())::text
        WHEN 1 THEN ('{Crimson,Azure,Emerald,Golden,Silver,Obsidian}'::text[])[(random() * 5)::int + 1] || ' ' || ('{Sky,Sea,Forest,Mountain,Desert,River}'::text[])[(random() * 5)::int + 1] || ' ' || (row_number() over ())::text
        WHEN 2 THEN ('{Midnight,Twilight,Dawn,Sunset,Noon}'::text[])[(random() * 4)::int + 1] || ' ' || ('{Project,Collective,Assembly,Union}'::text[])[(random() * 3)::int + 1] || ' ' || (row_number() over ())::text
        WHEN 3 THEN ('{Electric,Digital,Analog,Neon,Urban}'::text[])[(random() * 4)::int + 1] || ' ' || ('{Dream,Reality,Fantasy,Nightmare}'::text[])[(random() * 3)::int + 1] || ' ' || (row_number() over ())::text
        WHEN 4 THEN ('{Northern,Southern,Eastern,Western}'::text[])[(random() * 3)::int + 1] || ' ' || ('{Lights,Winds,Stars,Oceans}'::text[])[(random() * 3)::int + 1] || ' ' || (row_number() over ())::text
        WHEN 5 THEN ('{Silent,Loud,Quiet,Noisy}'::text[])[(random() * 3)::int + 1] || ' ' || ('{Revolution,Evolution,Solution,Resolution}'::text[])[(random() * 3)::int + 1] || ' ' || (row_number() over ())::text
        ELSE ('{Atomic,Quantum,Neural,Cyber,Proto}'::text[])[(random() * 4)::int + 1] || ' ' || ('{Wave,Pulse,Signal,Field,Matrix}'::text[])[(random() * 4)::int + 1] || ' ' || (row_number() over ())::text
        END,
    (random() * 199 + 1)::int, -- coordinates_id
    ('{PROGRESSIVE_ROCK,SOUL,ROCK,POST_ROCK,PUNK_ROCK,POST_PUNK}'::text[])[(random() * 5)::int + 1],
    (random() * 7 + 1)::bigint,
    (random() * 50 + 1)::bigint,
    CASE (random() * 3)::int
        WHEN 0 THEN 'Innovative band known for their unique sound and powerful performances'
        WHEN 1 THEN 'Rising stars in the music scene with a dedicated fan base'
        WHEN 2 THEN 'Experimental group pushing the boundaries of their genre'
        ELSE 'Critically acclaimed ensemble with multiple award nominations'
        END,
    (random() * 199 + 1)::int, -- best_album_id
    (random() * 15 + 1)::bigint,
    timestamp '1970-01-01' + random() * (timestamp '2020-01-01' - timestamp '1970-01-01'), -- establishment_date
    (random() * 199 + 1)::int, -- front_man_id
    'system',
    base_created,
    'system',
    base_created + (random() * 30)::int * INTERVAL '1 day'
FROM (
         SELECT
                     CURRENT_TIMESTAMP - (random() * 300)::integer * INTERVAL '1 day' AS base_created
         FROM generate_series(1, 200)
     ) s;

-- Заполнение best_band_award (требует music_band)
INSERT INTO best_band_award (band_id, genre, created_by, created_date, last_modified_by, last_modified_date)
SELECT
    (random() * 199 + 1)::int, -- band_id
    ('{PROGRESSIVE_ROCK,SOUL,ROCK,POST_ROCK,PUNK_ROCK,POST_PUNK}'::text[])[(random() * 5)::int + 1],
    'system',
    base_created,
    'system',
    base_created + (random() * 30)::int * INTERVAL '1 day'
FROM (
         SELECT
                     CURRENT_TIMESTAMP - (random() * 300)::integer * INTERVAL '1 day' AS base_created
         FROM generate_series(1, 300)
     ) s;