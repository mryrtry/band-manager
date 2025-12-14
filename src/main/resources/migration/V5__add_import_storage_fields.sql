ALTER TABLE import_operations
    ADD COLUMN IF NOT EXISTS staging_object_key VARCHAR(255),
    ADD COLUMN IF NOT EXISTS storage_object_key VARCHAR(255),
    ADD COLUMN IF NOT EXISTS content_type VARCHAR(150),
    ADD COLUMN IF NOT EXISTS file_size BIGINT;
