package br.unb.sds.gds2ephem.application.model;

import br.unb.sds.gds2ephem.application.model.exceptions.EventoIntegracaoValidacaoException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.Errors;

import static br.unb.sds.gds2ephem.configs.ErrorMessagesKeys.TEMPLATE_NAO_ENCONTRADO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoIntegracaoValidatorTest {
    private EventoIntegracaoValidator validator;

    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        validator = new EventoIntegracaoValidator();
    }

    @Test
    @DisplayName("Deve retornar verdadeiro dado que suporta a classe EventoIntegracao")
    void supports_ValidClass_ReturnsTrue() {
        final var supports = validator.supports(EventoIntegracao.class);

        assertTrue(supports);
    }

    @Test
    @DisplayName("Deve retornar falso dado que nao suportar a classe Object")
    void supports_InvalidClass_ReturnsFalse() {
        final var supports = validator.supports(Object.class);

        assertFalse(supports);
    }

    @Test
    @DisplayName("Deve rejeitar na validacao dado que nao foi definido o template")
    void validate_NullEventoIntegracaoTemplate_RejectsValue() {
        final var eventoIntegracao = new EventoIntegracao();

        validator.validate(eventoIntegracao, errors);

        verify(errors).rejectValue(EventoIntegracaoValidator.EVENTO_INTEGRACAO_FIELD_NAME, TEMPLATE_NAO_ENCONTRADO);
    }

    @Test
    @DisplayName("Deve rejeitar na validacao dado que id do template não foi definido")
    void validate_NullEventoIntegracaoTemplateId_RejectsValue() {
        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setEventoIntegracaoTemplate(new EventoIntegracaoTemplate());

        validator.validate(eventoIntegracao, errors);

        verify(errors).rejectValue(EventoIntegracaoValidator.EVENTO_INTEGRACAO_FIELD_NAME, TEMPLATE_NAO_ENCONTRADO);
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve executar validacao sem erros")
    void deveExecutarSemErros() {
        final var inputStream = new ClassPathResource("schemas/schema.json").getInputStream();
        final var mapper = new ObjectMapper();
        final var jsonNodeSchema = mapper.readValue(inputStream, JsonNode.class);
        final var template = new EventoIntegracaoTemplate();
        template.setId(1L);
        template.setDefinition(jsonNodeSchema);
        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setEventoIntegracaoTemplate(template);
        final var jsonNodeData = mapper.readTree("{\"field_1\": true}");
        eventoIntegracao.setData(jsonNodeData);

        validator.validate(eventoIntegracao, errors);

        verify(errors, never()).rejectValue(anyString(), anyString(), any(), anyString());
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve lancar excecao dada execao de cast")
    void deveLancarExcecao() {
        final var eventoIntegracaoValidacaoException = assertThrows(EventoIntegracaoValidacaoException.class, () -> validator.validate(new Object(), errors));

        verify(errors, never()).rejectValue(anyString(), anyString(), any(), anyString());
        assertTrue(eventoIntegracaoValidacaoException.getMessage().contains("Object cannot be cast"));
    }
}