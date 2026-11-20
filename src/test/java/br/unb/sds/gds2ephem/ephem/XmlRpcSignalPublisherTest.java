package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoTemplate;
import br.unb.sds.gds2ephem.application.model.exceptions.EventoIntegracaoValidacaoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XmlRpcSignalPublisherTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private EphemPort ephemPort;
    @Mock
    private NarrativeSignalService narrativeSignalService;
    @Mock
    private EphemMapper ephemMapper;

    private XmlRpcSignalPublisher publisher;
    private EventoIntegracao evento;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        publisher = new XmlRpcSignalPublisher(ephemPort, narrativeSignalService, ephemMapper);
        final var template = new EventoIntegracaoTemplate();
        template.setDefinition(objectMapper.readTree("{\"$schema\": \"http://json-schema.org/draft-07/schema#\","
                + " \"type\": \"object\", \"required\": [\"signal_type\"],"
                + " \"properties\": {\"signal_type\": {\"type\": \"string\"}}}"));
        evento = new EventoIntegracao();
        evento.setEventoIntegracaoTemplate(template);
    }

    @SneakyThrows
    @Test
    @DisplayName("Dados validos: envia o mapeamento do template mais a descricao narrativa")
    void publica() {
        when(ephemMapper.mapearData(evento)).thenReturn(objectMapper.readTree("{\"signal_type\": \"opening\"}"));
        when(narrativeSignalService.buildNarrativeDescription(evento)).thenReturn("<p>narrativa</p>");
        when(ephemPort.criarSignal(eq(evento), any())).thenReturn(321L);

        assertThat(publisher.publicar(evento)).isEqualTo(321L);

        @SuppressWarnings("unchecked") final ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(ephemPort).criarSignal(eq(evento), captor.capture());
        assertThat(captor.getValue()).containsEntry("signal_type", "opening").containsEntry("description", "<p>narrativa</p>");
    }

    @SneakyThrows
    @Test
    @DisplayName("Dados que violam o JSON Schema do template: erro de validacao e nada e enviado ao ePHEM")
    void schemaInvalido() {
        when(ephemMapper.mapearData(evento)).thenReturn(objectMapper.readTree("{\"signal_type\": 1}"));

        assertThatThrownBy(() -> publisher.publicar(evento))
                .isInstanceOf(EventoIntegracaoValidacaoException.class)
                .hasMessageContaining("signal_type");
        verifyNoInteractions(ephemPort);
    }
}
