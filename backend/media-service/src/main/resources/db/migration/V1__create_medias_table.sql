CREATE TABLE medias (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    detected_content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    width INTEGER CHECK (
        width IS NULL
        OR width > 0
    ),
    height INTEGER CHECK (
        height IS NULL
        OR height > 0
    ),
    duration_ms BIGINT CHECK (
        duration_ms IS NULL
        OR duration_ms >= 0
    ),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);