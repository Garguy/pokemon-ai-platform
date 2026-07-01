-- Each "test" run is now a batch. Old constraint only allowed one test per user.
ALTER TABLE recommendations DROP CONSTRAINT IF EXISTS uq_recommendations_user_rank;

ALTER TABLE recommendations ADD COLUMN batch_id UUID;

-- Existing rows: treat each user's current recommendations as a single batch.
UPDATE recommendations r
SET batch_id = sub.bid
FROM (SELECT user_id, uuid_generate_v4() AS bid FROM recommendations GROUP BY user_id) sub
WHERE r.user_id = sub.user_id;

ALTER TABLE recommendations ALTER COLUMN batch_id SET NOT NULL;

ALTER TABLE recommendations
    ADD CONSTRAINT uq_recommendations_batch_rank UNIQUE (user_id, batch_id, rank);

CREATE INDEX idx_recommendations_user_generated
    ON recommendations (user_id, generated_at DESC);
