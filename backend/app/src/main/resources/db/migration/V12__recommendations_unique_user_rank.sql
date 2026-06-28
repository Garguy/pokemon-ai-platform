ALTER TABLE recommendations
    ADD CONSTRAINT uq_recommendations_user_rank UNIQUE (user_id, rank);
