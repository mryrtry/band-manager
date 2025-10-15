TRUNCATE TABLE music_band CASCADE;
TRUNCATE TABLE person CASCADE;
TRUNCATE TABLE album CASCADE;
TRUNCATE TABLE coordinates CASCADE;
TRUNCATE TABLE location CASCADE;


ALTER SEQUENCE location_id_seq RESTART WITH 1;
ALTER SEQUENCE coordinates_id_seq RESTART WITH 1;
ALTER SEQUENCE album_id_seq RESTART WITH 1;
ALTER SEQUENCE person_id_seq RESTART WITH 1;
ALTER SEQUENCE music_band_id_seq RESTART WITH 1;
ALTER SEQUENCE best_band_award_id_seq RESTART WITH 1;


INSERT INTO location (x, y, z)
SELECT (random() * 1000 - 500)::numeric(10, 2),
       (random() * 2000 - 1000)::numeric(10, 2),
       (random() * 5000)::numeric(10, 2)
FROM generate_series(1, 200);


INSERT INTO coordinates (x, y)
SELECT (random() * 180 - 90)::numeric(8, 4),
       (random() * 360 - 180)::numeric(9, 4)
FROM generate_series(1, 200);


INSERT INTO album (name, tracks, sales)
SELECT CASE (random() * 9)::int
           WHEN 0 THEN 'Echoes of ' ||
                       ('{Time,Destiny,Silence,Memory,Freedom,Hope,Dreams,Reality,Eternity}'::text[])[(random() * 8)::int + 1]
           WHEN 1 THEN
               'The ' || ('{Last,First,Final,Lost,Secret,Ancient,Modern,Digital}'::text[])[(random() * 7)::int + 1] ||
               ' ' || ('{Journey,Chapter,Whisper,Scream,Revolution,Evolution}'::text[])[(random() * 5)::int + 1]
           WHEN 2 THEN ('{Midnight,Sunrise,Twilight,Dawn,Dusk,Noon}'::text[])[(random() * 5)::int + 1] || ' ' ||
                       ('{Sessions,Chronicles,Stories,Memories}'::text[])[(random() * 3)::int + 1]
           WHEN 3 THEN ('{Electric,Acoustic,Digital,Analog,Neon,Urban}'::text[])[(random() * 5)::int + 1] || ' ' ||
                       ('{Dreams,Nights,Days,Waves,Currents}'::text[])[(random() * 4)::int + 1]
           WHEN 4 THEN 'Volume ' || (random() * 9 + 1)::int
           WHEN 5 THEN ('{Northern,Southern,Eastern,Western}'::text[])[(random() * 3)::int + 1] || ' ' ||
                       ('{Lights,Winds,Skies,Seas}'::text[])[(random() * 3)::int + 1]
           WHEN 6 THEN
               'The ' || ('{Red,Blue,Green,Black,White,Golden,Silver}'::text[])[(random() * 6)::int + 1] || ' ' ||
               ('{Album,Record,Collection,Anthology}'::text[])[(random() * 3)::int + 1]
           WHEN 7 THEN ('{Chronicles,Legends,Myths,Fables}'::text[])[(random() * 3)::int + 1] || ' of ' ||
                       ('{Tomorrow,Yesterday,Today,Forever}'::text[])[(random() * 3)::int + 1]
           ELSE 'Unknown Sessions ' || (random() * 1000)::int
           END,
       (random() * 20 + 1)::int,
       (random() * 100000000)::numeric(12, 0)
FROM generate_series(1, 200);


INSERT INTO person (name, eye_color, hair_color, location_id, weight, nationality)
SELECT CASE (random() * 4)::int
           WHEN 0 THEN
               ('{John,Michael,David,Robert,James,William,Richard,Thomas,Christopher,Daniel}'::text[])[(random() * 9)::int + 1] ||
               ' ' ||
               ('{Smith,Johnson,Williams,Brown,Jones,Miller,Davis,Garcia,Wilson,Anderson}'::text[])[(random() * 9)::int + 1]
           WHEN 1 THEN
               ('{Emma,Olivia,Ava,Isabella,Sophia,Charlotte,Mia,Amelia,Harper,Evelyn}'::text[])[(random() * 9)::int + 1] ||
               ' ' || ('{Taylor,Moore,Clark,Allen,Young,King,Wright,Scott,Green,Hall}'::text[])[(random() * 9)::int + 1]
           WHEN 2 THEN
               ('{Alex,Jordan,Casey,Riley,Avery,Cameron,Charlie,Finley,Harley,Skyler}'::text[])[(random() * 9)::int + 1] ||
               ' ' ||
               ('{Lee,Walker,Hill,Adams,Nelson,Baker,Carter,Mitchell,Perez,Roberts}'::text[])[(random() * 9)::int + 1]
           ELSE ('{Max,Luca,Noah,Ethan,Leo,Jack,Oscar,Archie,Henry,Jacob}'::text[])[(random() * 9)::int + 1] || ' ' ||
                ('{Harris,Martin,Thompson,White,Lopez,Clark,Rodriguez,Lewis,Lee,Walker}'::text[])[(random() * 9)::int + 1]
           END,
       ('{BLACK,ORANGE,BROWN,GREEN,BLUE}'::text[])[(random() * 4)::int + 1],
       ('{BLACK,ORANGE,BROWN,GREEN,BLUE}'::text[])[(random() * 4)::int + 1],
       (random() * 199 + 1)::int,
       (random() * 50 + 50)::numeric(4, 1),
       ('{FRANCE,INDIA,THAILAND,USA,UK}'::text[])[(random() * 4)::int + 1]
FROM generate_series(1, 200);


INSERT INTO music_band (name, coordinates_id, genre, number_of_participants, singles_count, description, best_album_id,
                        albums_count, establishment_date, front_man_id)
SELECT CASE (random() * 7)::int
           WHEN 0 THEN 'The ' ||
                       ('{Echoes,Voices,Shadows,Lights,Currents,Waves,Elements,Forces}'::text[])[(random() * 7)::int + 1]
           WHEN 1 THEN ('{Crimson,Azure,Emerald,Golden,Silver,Obsidian}'::text[])[(random() * 5)::int + 1] || ' ' ||
                       ('{Sky,Sea,Forest,Mountain,Desert,River}'::text[])[(random() * 5)::int + 1]
           WHEN 2 THEN ('{Midnight,Twilight,Dawn,Sunset,Noon}'::text[])[(random() * 4)::int + 1] || ' ' ||
                       ('{Project,Collective,Assembly,Union}'::text[])[(random() * 3)::int + 1]
           WHEN 3 THEN ('{Electric,Digital,Analog,Neon,Urban}'::text[])[(random() * 4)::int + 1] || ' ' ||
                       ('{Dream,Reality,Fantasy,Nightmare}'::text[])[(random() * 3)::int + 1]
           WHEN 4 THEN ('{Northern,Southern,Eastern,Western}'::text[])[(random() * 3)::int + 1] || ' ' ||
                       ('{Lights,Winds,Stars,Oceans}'::text[])[(random() * 3)::int + 1]
           WHEN 5 THEN ('{Silent,Loud,Quiet,Noisy}'::text[])[(random() * 3)::int + 1] || ' ' ||
                       ('{Revolution,Evolution,Solution,Resolution}'::text[])[(random() * 3)::int + 1]
           ELSE ('{Atomic,Quantum,Neural,Cyber,Proto}'::text[])[(random() * 4)::int + 1] || ' ' ||
                ('{Wave,Pulse,Signal,Field,Matrix}'::text[])[(random() * 4)::int + 1]
           END,
       (random() * 199 + 1)::int,
       ('{PROGRESSIVE_ROCK,SOUL,ROCK,POST_ROCK,PUNK_ROCK,POST_PUNK}'::text[])[(random() * 5)::int + 1],
       (random() * 7 + 1)::int,
       (random() * 50 + 1)::int,
       CASE (random() * 3)::int
           WHEN 0 THEN 'Innovative band known for their unique sound and powerful performances'
           WHEN 1 THEN 'Rising stars in the music scene with a dedicated fan base'
           WHEN 2 THEN 'Experimental group pushing the boundaries of their genre'
           ELSE 'Critically acclaimed ensemble with multiple award nominations'
           END,
       (random() * 199 + 1)::int,
       (random() * 15 + 1)::int,
       timestamp '1970-01-01' +
       random() * (timestamp '2020-01-01' - timestamp '1970-01-01'),
       (random() * 199 + 1)::int
FROM generate_series(1, 200);


INSERT INTO best_band_award (band_id, genre, created_at)
SELECT (random() * 199 + 1)::int,
       ('{PROGRESSIVE_ROCK,SOUL,ROCK,POST_ROCK,PUNK_ROCK,POST_PUNK}'::text[])[(random() * 5)::int + 1],
       timestamp '2020-01-01' +
       random() * (timestamp '2024-12-31' - timestamp '2020-01-01')
FROM generate_series(1, 300);