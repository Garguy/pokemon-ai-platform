INSERT INTO users (id, email, password_hash, created_at)
VALUES (
    gen_random_uuid(),
    'admin@pokemonai.com',
    '$2y$10$0jWuzM4VwzXpv8r6WwkB/.pbsIVkK8tRUAWq1O0L390qygCKJMNg.',
    NOW()
)
ON CONFLICT (email) DO NOTHING;
