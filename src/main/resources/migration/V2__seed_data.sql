INSERT INTO coordinates (x, y)
VALUES (10, 5.5);
INSERT INTO coordinates (x, y)
VALUES (50, 3.2);

INSERT INTO album (name, tracks, sales)
VALUES ('Best Hits', 12, 5000);

INSERT INTO album (name, tracks, sales)
VALUES ('Super Album', 10, 10000);

INSERT INTO location (x, y, z)
VALUES (1, 2, 3);

INSERT INTO location (x, y, z)
VALUES (10, 20, 30);

INSERT INTO person (name, eye_color, hair_color, weight, nationality, location_id)
VALUES ('John Doe', 'BLACK', 'ORANGE', 70.5, 'FRANCE', 1);

INSERT INTO person (name, eye_color, hair_color, weight, nationality, location_id)
VALUES ('Jane Doe', 'BROWN', 'BLACK', 55.0, 'INDIA', 2);

INSERT INTO music_band (name, coordinates_id, creation_date, genre, number_of_participants,
                        singles_count, description, best_album_id, albums_count, establishment_date, front_man_id)
VALUES ('My First Band', 1, CURRENT_TIMESTAMP, 'PROGRESSIVE_ROCK', 5,
        3, 'Our first band description', 1, 2, CURRENT_TIMESTAMP, 1);

INSERT INTO music_band (name, coordinates_id, creation_date, genre, number_of_participants,
                        singles_count, description, best_album_id, albums_count, establishment_date, front_man_id)
VALUES ('Second Band', 2, CURRENT_TIMESTAMP, 'SOUL', 4,
        2, 'Our second band description', 2, 1, CURRENT_TIMESTAMP, 2);
