package br.unb.sds.gds2ephem.ephemv2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Listas extraidas de cv.pheoc.com (nov/2026), com as grafias de la: sem til, abreviadas e com erros.
 */
@ExtendWith(MockitoExtension.class)
class EphemV2PlaceResolverTest {
    private static final String STATES = "[{\"id\":9816,\"name\":\"BOA VISTA\"},{\"id\":9817,\"name\":\"BRAVA\"},"
            + "{\"id\":9818,\"name\":\"FOGO\"},{\"id\":9819,\"name\":\"MAIO\"},{\"id\":9820,\"name\":\"SAL\"},"
            + "{\"id\":9821,\"name\":\"SANTIAGO\"},{\"id\":9822,\"name\":\"SANTO ANTAO\"},"
            + "{\"id\":9823,\"name\":\"SAO NICOLAU\"},{\"id\":9824,\"name\":\"SAO VICENTE\"}]";
    private static final String DISTRICTS = "["
            + d(444, 9821, "ASSOMADA") + "," + d(437, 9816, "BOA VISTA") + "," + d(438, 9817, "BRAVA") + ","
            + d(442, 9819, "MAIO") + "," + d(439, 9818, "MOSTEIROS") + "," + d(453, 9822, "P.NOVO") + ","
            + d(454, 9822, "PAUL") + "," + d(445, 9821, "PRAIA") + "," + d(455, 9822, "R.GRANDE") + ","
            + d(456, 9823, "RIBERIA BRAVA") + "," + d(446, 9821, "RIBERIA GRANDE DE SANTIAGO") + ","
            + d(440, 9818, "S.CATARINA") + "," + d(447, 9821, "S.CRUZ") + "," + d(448, 9821, "S.DOMINGOS") + ","
            + d(441, 9818, "S.FILIPE") + "," + d(449, 9821, "S.L.ÓRGÃOS") + "," + d(457, 9823, "S.MIGUEL") + ","
            + d(450, 9821, "S.S.MUNDO") + "," + d(458, 9824, "S.VICENTE") + "," + d(443, 9820, "SAL") + ","
            + d(451, 9821, "SÃO NICOLAU") + "," + d(452, 9821, "TARRAFAL") + "]";

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private EphemV2Client client;
    private MutableClock clock;
    private EphemV2PlaceResolver resolver;

    private static String d(int id, int stateId, String name) {
        return "{\"id\":" + id + ",\"state_id\":" + stateId + ",\"name\":\"" + name + "\"}";
    }

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-11-19T12:00:00Z"));
        resolver = new EphemV2PlaceResolver(client, Duration.ofHours(6), clock);
        lenient().when(client.listarEstados("CV")).thenAnswer(inv -> lista(STATES));
        lenient().when(client.listarDistritos("CV")).thenAnswer(inv -> lista(DISTRICTS));
    }

    @SneakyThrows
    private List<JsonNode> lista(String json) {
        final var rows = new ArrayList<JsonNode>();
        objectMapper.readTree(json).forEach(rows::add);
        return rows;
    }

    @SneakyThrows
    private ObjectNode resolver(String geo) {
        final var node = (ObjectNode) objectMapper.readTree(geo);
        resolver.resolver(node, 1L);
        return node;
    }

    @ParameterizedTest(name = "{0} / {1} -> {2} / {3}")
    @DisplayName("Nomes como o app envia casam com as grafias do ePHEM")
    @CsvSource({
            "Santo Antão, Porto Novo, 9822, 453",
            "Santo Antão, Ribeira Grande, 9822, 455",
            "Santiago, Praia, 9821, 445",
            "SANTIAGO, praia, 9821, 445",
            "Santiago, Praiaa, 9821, 445",
            "Santiago, São Lourenço dos Órgãos, 9821, 449",
            "Santiago, São Salvador do Mundo, 9821, 450",
            "Santiago, Santa Cruz, 9821, 447",
            "Santiago, Ribeira Grande de Santiago, 9821, 446",
            "São Nicolau, Ribeira Brava, 9823, 456",
            "Fogo, São Filipe, 9818, 441",
            "Sao Vicente, Sao Vicente, 9824, 458",
    })
    void casamentos(String estado, String municipio, long stateId, long districtId) {
        final var geo = resolver("{\"country\":\"CV\",\"state\":\"" + estado + "\",\"district\":\"" + municipio + "\"}");

        assertThat(geo.toString()).isEqualTo("{\"country\":\"CV\",\"state\":" + stateId + ",\"district\":" + districtId + "}");
    }

    @Test
    @DisplayName("Municipio e procurado so dentro do estado: 'São Nicolau' de Santiago nao e a ilha")
    void municipioDentroDoEstado() {
        assertThat(resolver("{\"country\":\"CV\",\"state\":\"Santiago\",\"district\":\"São Nicolau\"}").toString())
                .isEqualTo("{\"country\":\"CV\",\"state\":9821,\"district\":451}");
    }

    @Test
    @DisplayName("Sem estado, o municipio unico no pais define tambem o estado")
    void estadoInferidoDoMunicipio() {
        assertThat(resolver("{\"country\":\"CV\",\"district\":\"Mosteiros\"}").toString())
                .isEqualTo("{\"country\":\"CV\",\"district\":439,\"state\":9818}");
    }

    @Test
    @DisplayName("Estado desconhecido sai; o municipio ainda pode salvar o lugar")
    void estadoDesconhecido() {
        assertThat(resolver("{\"country\":\"CV\",\"state\":\"Ilha Errada\",\"district\":\"Paul\"}").toString())
                .isEqualTo("{\"country\":\"CV\",\"district\":454,\"state\":9822}");
    }

    @Test
    @DisplayName("Municipio desconhecido sai, estado e mantido")
    void municipioDesconhecido() {
        assertThat(resolver("{\"country\":\"CV\",\"state\":\"Santiago\",\"district\":\"Lisboa\"}").toString())
                .isEqualTo("{\"country\":\"CV\",\"state\":9821}");
    }

    @Test
    @DisplayName("Exato ganha do aproximado; dois aproximados = ambiguo, nada e enviado")
    void ambiguo() {
        assertThat(EphemV2PlaceResolver.unico(lista(DISTRICTS), "S. Domingos")).map(n -> n.path("id").asInt()).contains(448);
        assertThat(EphemV2PlaceResolver.unico(lista("[{\"id\":1,\"name\":\"S.CRUZ\"},{\"id\":2,\"name\":\"SANTA CRUZ\"}]"), "Santa Cruz"))
                .map(n -> n.path("id").asInt()).contains(2);
        assertThat(EphemV2PlaceResolver.unico(lista("[{\"id\":1,\"name\":\"S.CRUZ\"},{\"id\":2,\"name\":\"SANTA CRUZ\"}]"), "Santo Cruz"))
                .isEmpty();
    }

    @Test
    @DisplayName("Palavras curtas nao aceitam erro de digitacao (SAL nao vira MAL)")
    void palavrasCurtas() {
        assertThat(EphemV2PlaceResolver.palavrasParecidas("SAL", "MAL")).isFalse();
        assertThat(EphemV2PlaceResolver.palavrasParecidas("RIBEIRA", "RIBERIA")).isTrue();
        assertThat(EphemV2PlaceResolver.palavrasParecidas("PRAIA", "PRAIAAA")).isFalse();
    }

    @Test
    @DisplayName("Listas ficam em cache ate o TTL e podem ser invalidadas")
    void cache() {
        resolver("{\"country\":\"CV\",\"state\":\"Santiago\"}");
        resolver("{\"country\":\"CV\",\"state\":\"Fogo\"}");
        verify(client, times(1)).listarEstados("CV");

        clock.avancar(Duration.ofHours(7));
        resolver("{\"country\":\"CV\",\"state\":\"Santiago\"}");
        verify(client, times(2)).listarEstados("CV");

        resolver.invalidar("CV");
        resolver("{\"country\":\"CV\",\"state\":\"Santiago\"}");
        verify(client, times(3)).listarEstados("CV");
    }

    @Test
    @DisplayName("Falha ao carregar as listas: geo segue com os nomes")
    void falhaNaCarga() {
        when(client.listarEstados("CV")).thenThrow(new IllegalStateException("timeout"));

        assertThat(resolver("{\"country\":\"CV\",\"state\":\"Santo Antão\"}").toString())
                .isEqualTo("{\"country\":\"CV\",\"state\":\"Santo Antão\"}");
    }

    @Test
    @DisplayName("Sem estado/municipio, ou pais fora do ISO, a API nao e consultada")
    void nadaAResolver() {
        resolver("{\"country\":\"CV\"}");
        resolver("{\"country\":\"Narnia\",\"state\":\"X\"}");
        verifyNoInteractions(client);
    }

    private static final class MutableClock extends Clock {
        private Instant agora;

        private MutableClock(Instant agora) {
            this.agora = agora;
        }

        void avancar(Duration duracao) {
            agora = agora.plus(duracao);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
