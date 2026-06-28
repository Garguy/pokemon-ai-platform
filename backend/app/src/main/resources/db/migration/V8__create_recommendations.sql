CREATE TABLE recommendations (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pokemon_id   UUID NOT NULL REFERENCES pokemon(id) ON DELETE CASCADE,
    match_score  NUMERIC(7,6) NOT NULL,
    rank         SMALLINT NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_recommendations_user_score
    ON recommendations (user_id, match_score DESC);
