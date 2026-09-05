CREATE TABLE template_images (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255),
    content_type VARCHAR(100),
    data BYTEA,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
