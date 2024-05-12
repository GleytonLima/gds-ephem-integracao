CREATE TABLE configuracao_sistema
(
    id                  BIGSERIAL PRIMARY KEY,
    ephem_admin_user_id BIGINT,
    ephem_gds_user_id   BIGINT,
    ephem_admin_api_key VARCHAR(255),
    created_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    updated_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

INSERT INTO configuracao_sistema (ephem_admin_user_id, ephem_gds_user_id, ephem_admin_api_key, created_at, updated_at)
VALUES (1, 2, 'abc123', now(), now());