INSERT INTO pokemon_traits (pokemon_id, trait_name, trait_score)
SELECT p.id, t.trait_name, 0.5
FROM pokemon p
CROSS JOIN (VALUES
    ('ENERGY'),
    ('CURIOSITY'),
    ('LEADERSHIP'),
    ('LOYALTY'),
    ('RISK'),
    ('CREATIVITY')
) AS t(trait_name)
ON CONFLICT (pokemon_id, trait_name) DO NOTHING;
