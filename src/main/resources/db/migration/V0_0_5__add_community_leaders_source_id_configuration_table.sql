ALTER TABLE configuracao_sistema ADD COLUMN community_leaders_source_id BIGINT;

UPDATE configuracao_sistema SET community_leaders_source_id = 1 WHERE id = 1;

