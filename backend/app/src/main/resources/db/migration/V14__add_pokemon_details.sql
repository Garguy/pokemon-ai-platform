ALTER TABLE pokemon
    ADD COLUMN types            VARCHAR(64),
    ADD COLUMN height           INTEGER,
    ADD COLUMN weight           INTEGER,
    ADD COLUMN category         VARCHAR(64),
    ADD COLUMN hp               INTEGER,
    ADD COLUMN attack           INTEGER,
    ADD COLUMN defense          INTEGER,
    ADD COLUMN special_attack   INTEGER,
    ADD COLUMN special_defense  INTEGER,
    ADD COLUMN speed            INTEGER;
