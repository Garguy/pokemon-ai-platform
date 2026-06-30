INSERT INTO users (id, email, password_hash, created_at)
VALUES (
    gen_random_uuid(),
    'admin@pokemonai.com',
    '$2a$10$nB9/BRCjkTbkiKPvYQ3EPeSxPx2hlHsL/BpdoUwG.sFLX53.SPbyO',
    NOW()
)
ON CONFLICT (email) DO NOTHING;
