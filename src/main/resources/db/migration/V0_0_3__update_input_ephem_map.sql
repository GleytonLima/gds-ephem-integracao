UPDATE evento_integracao_template
SET input_ephem_map = '{"state_id": {"from": ["evento_estado_ocorrencia"], "type": "model", "model_name": "res.country.state", "default_value": "0", "model_property_filter": "name"}, "country_id": {"from": ["evento_pais_ocorrencia"], "type": "model", "model_name": "res.country", "default_value": "0", "model_property_filter": "name"}, "report_date": {"from": ["evento_data_ocorrencia"], "type": "date", "from_format": "dd-MM-yyyy", "default_value": "@today"}, "signal_type": {"from": [], "type": "string", "default_value": "opening"}, "district_ids": {"from": ["evento_municipio_ocorrencia"], "type": "model_array", "model_name": "res.country.state.district", "default_value": "@search(''Nova Localidade'')", "model_property_filter": "name"}, "incident_date": {"from": ["evento_data_ocorrencia"], "type": "date", "from_format": "dd-MM-yyyy", "default_value": "@today"}, "confidentiality": {"from": [], "type": "string", "default_value": "everyone"}, "general_hazard_id": {"from": [], "type": "integer", "default_value": "0"}, "specific_hazard_id": {"from": [], "type": "integer", "default_value": "0"}}'
WHERE id = 1;
