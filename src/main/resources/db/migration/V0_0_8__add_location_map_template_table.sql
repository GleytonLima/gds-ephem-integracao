ALTER TABLE evento_integracao_template ADD COLUMN location_map jsonb;
UPDATE evento_integracao_template
SET location_map = '{"country_source_field": "evento_pais_ocorrencia", "state_source_field": "evento_estado_ocorrencia", "district_source_field": "evento_municipio_ocorrencia"}'
WHERE id = 1;
