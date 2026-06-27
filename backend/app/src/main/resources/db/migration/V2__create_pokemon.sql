CREATE TABLE pokemon (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    external_id INTEGER NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    image_url   VARCHAR(500),
    synced_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
