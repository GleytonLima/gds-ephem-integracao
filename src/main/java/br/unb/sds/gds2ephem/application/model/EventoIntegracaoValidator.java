package br.unb.sds.gds2ephem.application.model;

import br.unb.sds.gds2ephem.application.model.exceptions.EventoIntegracaoValidacaoException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static br.unb.sds.gds2ephem.configs.ErrorMessagesKeys.DADOS_INVALIDOS;
import static br.unb.sds.gds2ephem.configs.ErrorMessagesKeys.TEMPLATE_NAO_ENCONTRADO;
import static java.util.Objects.isNull;

@Slf4j
@Component("beforeCreateEventoIntegracaoValidator")
public class EventoIntegracaoValidator implements Validator {

    public static final String DATA_FIELD_NAME = "data";
    public static final String SCHEMA_PATH = "schemas/schema.json";
    public static final String EVENTO_INTEGRACAO_FIELD_NAME = "eventoIntegracaoTemplate";
    public static final String ERRO_INESPERADO_NA_VALIDACAO = "erro inesperado na validacao";

    @Override
    public boolean supports(Class<?> clazz) {
        return EventoIntegracao.class.equals(clazz);
    }

    @Override
    public void validate(Object eventoIntegracaoObject, Errors errors) {
        log.info("Iniciando validação dos dados para o eventoIntegracaoObject {}", eventoIntegracaoObject);
        try {
            final var eventoIntegracao = ((EventoIntegracao) eventoIntegracaoObject);
            if (isNull(eventoIntegracao.getEventoIntegracaoTemplate()) || isNull(eventoIntegracao.getEventoIntegracaoTemplate().getId())) {
                errors.rejectValue(EVENTO_INTEGRACAO_FIELD_NAME, TEMPLATE_NAO_ENCONTRADO);
                return;
            }

            final var factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            final var jsonSchema = factory.getSchema(eventoIntegracao.getEventoIntegracaoTemplate().getDefinition());

            final var errosSchema = jsonSchema.validate(eventoIntegracao.getData());

            for (ValidationMessage validationMessage : errosSchema) {
                errors.rejectValue(DATA_FIELD_NAME, DADOS_INVALIDOS, new Object[]{validationMessage.getMessage()}, ERRO_INESPERADO_NA_VALIDACAO);
            }
            if (errors.hasFieldErrors()) {
                log.error("Erro ao processar os dados {}: {}", eventoIntegracaoObject, errors);
            }
        } catch (Exception e) {
            log.error("erro ao processa validacao: {}", e.getMessage());
            throw new EventoIntegracaoValidacaoException(e.getMessage());
        }
    }
}
