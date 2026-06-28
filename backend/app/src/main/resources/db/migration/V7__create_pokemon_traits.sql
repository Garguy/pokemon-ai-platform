CREATE TABLE pokemon_traits (
    pokemon_id  UUID NOT NULL REFERENCES pokemon(id) ON DELETE CASCADE,
    trait_name  VARCHAR(20) NOT NULL,
    trait_score NUMERIC(5,4) NOT NULL CHECK (trait_score BETWEEN 0 AND 1),
    PRIMARY KEY (pokemon_id, trait_name)
);
