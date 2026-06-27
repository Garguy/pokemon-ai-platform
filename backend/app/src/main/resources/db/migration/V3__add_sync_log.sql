CREATE TABLE sync_log (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    started_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at TIMESTAMP WITH TIME ZONE,
    synced_count INTEGER,
    status      VARCHAR(20) NOT NULL,
    error       TEXT
);
