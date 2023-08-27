package br.unb.sds.gds2ephem.application.model;

import br.unb.sds.gds2ephem.application.model.exceptions.EventoIntegracaoValidacaoException;
import br.unb.sds.gds2ephem.ephem.EphemMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static br.unb.sds.gds2ephem.configs.ErrorMessagesKeys.DADOS_INVALIDOS;
import static br.unb.sds.gds2ephem.configs.ErrorMessagesKeys.TEMPLATE_NAO_ENCONTRADO;
import static java.util.Objects.isNull;

@Slf4j
@Component("beforeCreateEventoIntegracaoValidator")
public class EventoIntegracaoValidator implements Validator {    public static final String EVENTO_INTEGRACAO_FIELD_NAME = "eventoIntegracaoTemplate";

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return EventoIntegracao.class.equals(clazz);
    }

    @Override
    public void validate(@NonNull Object eventoIntegracaoObject, @NonNull Errors errors) {
        log.info("Iniciando validação dos dados para o eventoIntegracaoObject {}", eventoIntegracaoObject);
        try {
            final var eventoIntegracao = ((EventoIntegracao) eventoIntegracaoObject);
            if (isNull(eventoIntegracao.getEventoIntegracaoTemplate()) || isNull(eventoIntegracao.getEventoIntegracaoTemplate().getId())) {
                errors.rejectValue(EVENTO_INTEGRACAO_FIELD_NAME, TEMPLATE_NAO_ENCONTRADO);
            }
        } catch (Exception e) {
            log.error("erro ao processa validacao: {}", e.getMessage());
            throw new EventoIntegracaoValidacaoException(e.getMessage());
        }
    }
}
