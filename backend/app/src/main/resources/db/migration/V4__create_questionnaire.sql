CREATE TABLE questions (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    text           TEXT NOT NULL,
    trait_category VARCHAR(20) NOT NULL,
    weight         NUMERIC(4,2) NOT NULL DEFAULT 1.0,
    display_order  INTEGER NOT NULL
);

CREATE TABLE user_answers (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_id  UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    answer_value SMALLINT NOT NULL CHECK (answer_value BETWEEN 1 AND 5),
    answered_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_question UNIQUE (user_id, question_id)
);
