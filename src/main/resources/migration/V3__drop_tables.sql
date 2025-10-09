-- Отключаем проверку внешних ключей для безопасного удаления
SET session_replication_role = 'replica';

-- Удаляем таблицы в правильном порядке зависимостей
DROP TABLE IF EXISTS music_band CASCADE;
DROP TABLE IF EXISTS person CASCADE;
DROP TABLE IF EXISTS album CASCADE;
DROP TABLE IF EXISTS coordinates CASCADE;
DROP TABLE IF EXISTS location CASCADE;

-- Удаляем последовательности
DROP SEQUENCE IF EXISTS location_id_seq CASCADE;
DROP SEQUENCE IF EXISTS coordinates_id_seq CASCADE;
DROP SEQUENCE IF EXISTS album_id_seq CASCADE;
DROP SEQUENCE IF EXISTS person_id_seq CASCADE;
DROP SEQUENCE IF EXISTS music_band_id_seq CASCADE;

-- Удаляем представления
DROP VIEW IF EXISTS band_details CASCADE;

-- Удаляем функции
DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;

-- Включаем проверку внешних ключей обратно
SET session_replication_role = 'origin';