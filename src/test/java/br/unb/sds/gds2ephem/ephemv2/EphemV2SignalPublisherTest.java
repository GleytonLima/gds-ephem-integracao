package br.unb.sds.gds2ephem.ephemv2;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.ephem.NarrativeSignalService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EphemV2SignalPublisherTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EphemV2Client client;
    @Mock
    private EphemV2SignalMapper mapper;
    @Mock
    private EphemV2PlaceResolver placeResolver;
    @Mock
    private NarrativeSignalService narrativeSignalService;

    private EphemV2SignalPublisher publisher;
    private final EventoIntegracao evento = new EventoIntegracao();
    /**
     * Copias do payload a cada chamada: o publisher altera o mesmo objeto entre tentativas.
     */
    private final List<String> enviados = new ArrayList<>();

    @BeforeEach
    void setUp() {
        publisher = new EphemV2SignalPublisher(client, mapper, placeResolver, narrativeSignalService);
        evento.setId(7);
        lenient().when(narrativeSignalService.buildNarrativeDescription(evento)).thenReturn("narrativa");
    }

    @SneakyThrows
    private ObjectNode payload(String geo) {
        return (ObjectNode) objectMapper.readTree("{\"external_id\":\"GDS-7\",\"geo\":" + geo + "}");
    }

    @SneakyThrows
    private JsonNode signal() {
        return objectMapper.readTree("{\"id\":555,\"external_id\":\"GDS-7\",\"signal_seq\":\"Sg1\"}");
    }

    @SneakyThrows
    private EphemV2ValidationException erro(String campo) {
        return new EphemV2ValidationException(objectMapper.readTree(
                "{\"message\":\"x\",\"details\":[{\"field\":\"" + campo + "\",\"issue\":\"unknown\"}]}"));
    }

    @Test
    @DisplayName("Devolve o id interno do signal criado")
    void publica() {
        when(mapper.mapear(evento, "narrativa")).thenReturn(payload("{\"country\":\"BR\"}"));
        when(client.criarSignal(any())).thenReturn(signal());

        assertThat(publisher.publicar(evento)).isEqualTo(555L);
    }

    @Test
    @DisplayName("Municipio recusado: reenvia sem o municipio, mantendo estado e pais")
    void fallbackMunicipio() {
        when(mapper.mapear(evento, "narrativa"))
                .thenReturn(payload("{\"country\":\"BR\",\"state\":\"Amazonas\",\"district\":\"Manuas\"}"));
        when(client.criarSignal(any())).thenAnswer(inv -> {
            enviados.add(inv.getArgument(0).toString());
            if (enviados.size() == 1) {
                throw erro("geo.district");
            }
            return signal();
        });

        assertThat(publisher.publicar(evento)).isEqualTo(555L);
        assertThat(enviados.get(1)).contains("\"geo\":{\"country\":\"BR\",\"state\":\"Amazonas\"}");
        // o 422 pode vir de um id que mudou no servidor: a lista em cache e descartada
        verify(placeResolver).invalidar("BR");
    }

    @Test
    @DisplayName("O geo passa pelo resolvedor de lugares antes do envio")
    void resolveLugarAntesDoEnvio() {
        when(mapper.mapear(evento, "narrativa"))
                .thenReturn(payload("{\"country\":\"CV\",\"state\":\"Santo Antão\"}"));
        doAnswer(inv -> ((ObjectNode) inv.getArgument(0)).put("state", 9822)).when(placeResolver).resolver(any(), eq(7L));
        when(client.criarSignal(any())).thenAnswer(inv -> {
            enviados.add(inv.getArgument(0).toString());
            return signal();
        });

        publisher.publicar(evento);

        assertThat(enviados.get(0)).contains("\"geo\":{\"country\":\"CV\",\"state\":9822}");
    }

    @Test
    @DisplayName("Sem geo, o resolvedor nao e chamado")
    void semGeoNaoResolve() {
        when(mapper.mapear(evento, "narrativa")).thenReturn((ObjectNode) objectMapper.createObjectNode().put("external_id", "GDS-7"));
        when(client.criarSignal(any())).thenReturn(signal());

        publisher.publicar(evento);

        verifyNoInteractions(placeResolver);
    }

    @Test
    @DisplayName("Pais recusado: reenvia sem geo nenhum")
    void fallbackPais() {
        when(mapper.mapear(evento, "narrativa"))
                .thenReturn(payload("{\"country\":\"Narnia\",\"state\":\"X\"}"));
        when(client.criarSignal(any())).thenAnswer(inv -> {
            enviados.add(inv.getArgument(0).toString());
            if (enviados.size() == 1) {
                throw erro("geo.country");
            }
            return signal();
        });

        publisher.publicar(evento);

        assertThat(enviados.get(1)).doesNotContain("geo");
    }

    @Test
    @DisplayName("422 fora de geo e repassado sem nova tentativa")
    void erroForaDeGeo() {
        when(mapper.mapear(evento, "narrativa")).thenReturn(payload("{\"country\":\"BR\"}"));
        when(client.criarSignal(any())).thenThrow(erro("health_interface"));

        assertThatThrownBy(() -> publisher.publicar(evento)).isInstanceOf(EphemV2ValidationException.class);
        verify(client, times(1)).criarSignal(any());
    }

    @Test
    @DisplayName("removerNivelGeo nao remove nada quando o nivel ja nao existe")
    void removerNivelInexistente() {
        final var semGeo = (ObjectNode) objectMapper.createObjectNode().put("external_id", "GDS-7");
        assertThat(EphemV2SignalPublisher.removerNivelGeo(semGeo, "district")).isFalse();
        assertThat(EphemV2SignalPublisher.removerNivelGeo(payload("{\"country\":\"BR\"}"), "bairro")).isFalse();
    }
}
