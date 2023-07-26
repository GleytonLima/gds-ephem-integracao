create table evento_integracao_template
(
    id         bigserial not null primary key,
    definition jsonb     not null,
    created_at timestamp,
    updated_at timestamp
);

create table evento_integracao
(
    id                       bigserial    not null primary key,
    template_id              bigint references evento_integracao_template (id),
    data                     jsonb        not null,
    aditional_data           jsonb,
    status                   varchar(100) not null default 'CRIADO',
    status_message           varchar(255) not null,
    signal_id                bigint,
    event_source_id          varchar(100),
    event_source_location    varchar(100),
    event_source_location_id bigint,
    user_id                  bigint       not null,
    user_email               varchar(100) not null,
    created_at               timestamp,
    updated_at               timestamp
);
CREATE INDEX idx_signal_id ON evento_integracao (signal_id);
INSERT INTO evento_integracao_template (definition, created_at, updated_at)
VALUES ('{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Product",
  "description": "Esquema que representa um''signal''do Ephem(Odoo)",
  "type": "object",
  "properties": {
    "general_hazard_id": {
      "type": "integer"
    },
    "confidentiality": {
      "type": "string",
      "enum": [
        "leadership",
        "pheoc",
        "ministries",
        "partners",
        "everyone"
      ]
    },
    "specific_hazard_id": {
      "type": "integer"
    },
    "state_id": {
      "type": "boolean"
    },
    "signal_type": {
      "type": "string",
      "enum": [
        "opening",
        "update",
        "related"
      ]
    },
    "report_date": {
      "type": "string",
      "format": "date"
    },
    "incident_date": {
      "type": "string",
      "format": "date"
    }
  },
  "required": [
    "general_hazard_id",
    "confidentiality",
    "specific_hazard_id",
    "state_id",
    "signal_type",
    "report_date",
    "incident_date"
  ]
}', NOW(), NOW());
