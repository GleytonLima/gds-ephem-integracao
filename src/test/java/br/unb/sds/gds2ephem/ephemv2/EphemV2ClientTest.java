package br.unb.sds.gds2ephem.ephemv2;

import br.unb.sds.gds2ephem.application.model.exceptions.EphemAuthException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class EphemV2ClientTest {
    private static final String BASE = "https://ephem.test/api/v2";
    private static final String CREATED = "{\"data\":{\"id\":14870,\"external_id\":\"GDS-1\",\"signal_seq\":\"Sg00000261\",\"status\":\"info\"},\"meta\":{\"request_id\":\"abc\"}}";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockRestServiceServer server;
    private EphemV2Client client;
    private List<Duration> esperas;

    @BeforeEach
    void setUp() {
        final var restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        final var properties = new EphemV2Properties();
        properties.setBaseUrl(BASE + "/");
        properties.setApiKey("key-123");
        properties.setMaxRetries(2);
        esperas = new ArrayList<>();
        client = new EphemV2Client(restTemplate, properties, objectMapper, esperas::add);
    }

    @Test
    @DisplayName("POST /signals envia bearer e JSON e devolve o objeto data")
    void criarSignal() throws Exception {
        server.expect(requestTo(BASE + "/signals"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer key-123"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.external_id").value("GDS-1"))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(CREATED));

        final var signal = client.criarSignal(objectMapper.readTree("{\"external_id\":\"GDS-1\"}"));

        assertThat(signal.path("id").asLong()).isEqualTo(14870L);
        assertThat(signal.path("signal_seq").asText()).isEqualTo("Sg00000261");
        server.verify();
    }

    @Test
    @DisplayName("422 vira EphemV2ValidationException com o campo do erro, sem retry")
    void validacao() {
        server.expect(times(1), requestTo(BASE + "/signals"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"code\":\"validation_error\",\"message\":\"'Centro' matches nothing\","
                                + "\"details\":[{\"field\":\"geo.district\",\"issue\":\"unknown\"}],\"request_id\":\"r1\"}}"));

        assertThatThrownBy(() -> client.criarSignal(objectMapper.createObjectNode()))
                .isInstanceOfSatisfying(EphemV2ValidationException.class, e -> {
                    assertThat(e.getMessage()).contains("'Centro' matches nothing").contains("r1");
                    assertThat(e.firstFieldWithIssue()).contains("geo.district");
                });
        server.verify();
        assertThat(esperas).isEmpty();
    }

    @Test
    @DisplayName("403 vira EphemAuthException, sem retry")
    void semPermissao() {
        server.expect(times(1), requestTo(BASE + "/signals"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"code\":\"forbidden\",\"message\":\"Your API user is not permitted to perform this action.\",\"request_id\":\"r2\"}}"));

        assertThatThrownBy(() -> client.criarSignal(objectMapper.createObjectNode()))
                .isInstanceOf(EphemAuthException.class)
                .hasMessageContaining("403")
                .hasMessageContaining("not permitted")
                .hasMessageContaining("r2");
        server.verify();
    }

    @Test
    @DisplayName("429 espera o Retry-After e tenta de novo")
    void rateLimit() throws Exception {
        final var headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, "7");
        server.expect(requestTo(BASE + "/signals"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).headers(headers));
        server.expect(requestTo(BASE + "/signals"))
                .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(CREATED));

        final var signal = client.criarSignal(objectMapper.createObjectNode());

        assertThat(signal.path("id").asLong()).isEqualTo(14870L);
        assertThat(esperas).containsExactly(Duration.ofSeconds(7));
        server.verify();
    }

    @Test
    @DisplayName("5xx tenta de novo com backoff e desiste depois de maxRetries")
    void erroServidor() {
        server.expect(times(3), requestTo(BASE + "/signals"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.criarSignal(objectMapper.createObjectNode()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 tentativas");
        assertThat(esperas).containsExactly(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(4));
        server.verify();
    }

    @Test
    @DisplayName("GET /signals/{external_id} devolve vazio no 404")
    void consultarNaoEncontrado() {
        server.expect(requestTo(BASE + "/signals/GDS-9"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"code\":\"not_found\",\"message\":\"Not found\"}}"));

        assertThat(client.consultarSignal("GDS-9")).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("Listas de lugares percorrem todas as paginas")
    void listarPaginado() {
        final var pagina1 = new StringBuilder("{\"data\":[");
        for (int i = 0; i < EphemV2Client.PAGE_SIZE; i++) {
            pagina1.append(i == 0 ? "" : ",").append("{\"id\":").append(i).append('}');
        }
        pagina1.append("],\"meta\":{\"total\":").append(EphemV2Client.PAGE_SIZE + 1).append("}}");
        server.expect(requestTo(BASE + "/districts?country=CV&page_size=500&page=1"))
                .andRespond(withSuccess(pagina1.toString(), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/districts?country=CV&page_size=500&page=2"))
                .andRespond(withSuccess("{\"data\":[{\"id\":999}],\"meta\":{\"total\":501}}", MediaType.APPLICATION_JSON));

        final var distritos = client.listarDistritos("CV");

        assertThat(distritos).hasSize(EphemV2Client.PAGE_SIZE + 1);
        assertThat(distritos.get(EphemV2Client.PAGE_SIZE).path("id").asInt()).isEqualTo(999);
        server.verify();
    }

    @Test
    @DisplayName("Lista curta termina na primeira pagina")
    void listarEstadosUmaPagina() {
        server.expect(times(1), requestTo(BASE + "/states?country=CV&page_size=500&page=1"))
                .andRespond(withSuccess("{\"data\":[{\"id\":9821,\"name\":\"SANTIAGO\"}],\"meta\":{\"total\":1}}", MediaType.APPLICATION_JSON));

        assertThat(client.listarEstados("CV")).hasSize(1);
        server.verify();
    }
}
