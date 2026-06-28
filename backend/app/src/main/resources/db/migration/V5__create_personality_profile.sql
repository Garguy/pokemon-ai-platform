CREATE TABLE personality_profiles (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    energy       NUMERIC(5,4) NOT NULL DEFAULT 0,
    curiosity    NUMERIC(5,4) NOT NULL DEFAULT 0,
    leadership   NUMERIC(5,4) NOT NULL DEFAULT 0,
    loyalty      NUMERIC(5,4) NOT NULL DEFAULT 0,
    risk         NUMERIC(5,4) NOT NULL DEFAULT 0,
    creativity   NUMERIC(5,4) NOT NULL DEFAULT 0,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
