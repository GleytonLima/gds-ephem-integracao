package br.unb.sds.gds2ephem.ephemv2;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EphemV2SignalMapperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private EphemV2SignalMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EphemV2SignalMapper(new EphemV2Properties(), objectMapper);
    }

    @SneakyThrows
    private EventoIntegracao evento(String data) {
        final var template = new EventoIntegracaoTemplate();
        template.setLocationMap(objectMapper.readTree("{\"country_source_field\": \"evento_pais_ocorrencia\","
                + " \"state_source_field\": \"geo_state\", \"district_source_field\": \"geo_city\"}"));
        final var evento = new EventoIntegracao();
        evento.setId(42);
        evento.setEventoIntegracaoTemplate(template);
        evento.setData(objectMapper.readTree(data));
        evento.setCreatedAt(Instant.parse("2026-11-18T13:45:10.123456Z"));
        evento.setUserCountry("Brasil");
        evento.setUserEmail("reporter@example.org");
        evento.setUserPhone("+5561999999999");
        return evento;
    }

    @Test
    @DisplayName("Monta o corpo completo do POST /signals")
    void mapearCompleto() {
        final var body = mapper.mapear(evento("{\"evento_descricao\": \"Duas ou mais pessoas doentes\","
                + " \"evento_afetados\": \"Pessoas\", \"evento_data_ocorrencia\": \"22-08-2023\","
                + " \"geo_state\": \"Amazonas\", \"geo_city\": \"Manaus\"}"), "<p>narrativa</p>");

        assertThat(body.path("external_id").asText()).isEqualTo("GDS-42");
        assertThat(body.path("title").asText()).isEqualTo("Duas ou mais pessoas doentes");
        assertThat(body.path("health_interface").get(0).asText()).isEqualTo("Human");
        assertThat(body.path("description").asText()).isEqualTo("<p>narrativa</p>");
        assertThat(body.path("signal_type").asText()).isEqualTo("primary");
        assertThat(body.path("confidentiality").asInt()).isEqualTo(1);
        assertThat(body.path("privacy").asText()).isEqualTo("portal");
        assertThat(body.path("report_date").asText()).isEqualTo("2026-11-18T13:45:10Z");
        // meia-noite em Brasilia (UTC-3)
        assertThat(body.path("onset_date").asText()).isEqualTo("2023-08-22T03:00:00Z");
        assertThat(body.path("geo").toString()).isEqualTo("{\"country\":\"BR\",\"state\":\"Amazonas\",\"district\":\"Manaus\"}");
        assertThat(body.path("source").toString()).isEqualTo("{\"phone\":\"+5561999999999\",\"email\":\"reporter@example.org\"}");
        assertThat(body.path("external_user_email").asText()).isEqualTo("reporter@example.org");
    }

    @Test
    @DisplayName("Titulo e cortado em 50 caracteres e tem fallback quando nao ha descricao")
    void titulo() {
        final var longo = mapper.mapear(evento("{\"evento_descricao\": \"" + "x".repeat(80) + "\"}"), "d");
        assertThat(longo.path("title").asText()).hasSize(50).endsWith("...");

        final var vazio = mapper.mapear(evento("{}"), "d");
        assertThat(vazio.path("title").asText()).isEqualTo("Reporte GDS #42");
    }

    @Test
    @DisplayName("Afetados viram health_interface do ePHEM")
    void healthInterface() {
        assertThat(mapper.healthInterface(objectMapper.createObjectNode().put("evento_afetados", "Ambiente"))).isEqualTo("Environment");
        assertThat(mapper.healthInterface(objectMapper.createObjectNode().put("evento_afetados", "Animais"))).isEqualTo("Animal");
        assertThat(mapper.healthInterface(objectMapper.createObjectNode())).isEqualTo("Human");
    }

    @Test
    @DisplayName("Pais vira codigo ISO, com ou sem acento")
    void paisIso() {
        final var evento = evento("{}");
        evento.setUserCountry("Moçambique");
        assertThat(mapper.mapear(evento, "d").path("geo").path("country").asText()).isEqualTo("MZ");

        evento.setUserCountry("Cabo Verde");
        assertThat(mapper.mapear(evento, "d").path("geo").path("country").asText()).isEqualTo("CV");

        evento.setUserCountry("Narnia");
        assertThat(mapper.mapear(evento, "d").path("geo").path("country").asText()).isEqualTo("Narnia");
    }

    @Test
    @DisplayName("Sem telefone nem email valido, source usa o endereco configurado e nao envia external_user_email")
    void sourceFallback() {
        final var evento = evento("{}");
        evento.setUserPhone(" ");
        evento.setUserEmail("nao-e-email");

        final var body = mapper.mapear(evento, "d");

        assertThat(body.path("source").toString()).isEqualTo("{\"address\":\"https://gds.proepi.org.br\"}");
        assertThat(body.has("external_user_email")).isFalse();
    }

    @Test
    @DisplayName("Sem pais nem lugar, geo nao e enviado")
    void semGeo() {
        final var evento = evento("{}");
        evento.setUserCountry(null);

        assertThat(mapper.mapear(evento, "d").has("geo")).isFalse();
    }

    @Test
    @DisplayName("Data de ocorrencia ISO com offset e convertida para UTC; formato desconhecido e ignorado")
    void onsetDate() {
        assertThat(mapper.mapear(evento("{\"evento_data_ocorrencia\": \"2026-07-01T09:30:00-01:00\"}"), "d")
                .path("onset_date").asText()).isEqualTo("2026-07-01T10:30:00Z");
        assertThat(mapper.mapear(evento("{\"evento_data_ocorrencia\": \"ontem\"}"), "d").has("onset_date")).isFalse();
    }

    @Test
    @DisplayName("Usuario sem pais: fuso e geo vem do pais do formulario (caso do usuario criado pelo admin)")
    void fusoPeloPaisDoFormulario() {
        final var evento = evento("{\"evento_data_ocorrencia\": \"18-11-2026\", \"evento_pais_ocorrencia\": \"Cabo Verde\"}");
        evento.setUserCountry(null);

        final var body = mapper.mapear(evento, "d");

        // meia-noite em Cabo Verde (UTC-1), nao em Brasilia
        assertThat(body.path("onset_date").asText()).isEqualTo("2026-11-18T01:00:00Z");
        assertThat(body.path("geo").path("country").asText()).isEqualTo("CV");
    }

    @Test
    @DisplayName("Pais do usuario tem prioridade sobre o do formulario")
    void fusoPeloPaisDoUsuario() {
        final var evento = evento("{\"evento_data_ocorrencia\": \"18-11-2026\", \"evento_pais_ocorrencia\": \"Cabo Verde\"}");

        assertThat(mapper.mapear(evento, "d").path("onset_date").asText()).isEqualTo("2026-11-18T03:00:00Z");
    }

    @Test
    @DisplayName("Fuso ignora maiusculas e acentos; pais desconhecido usa o padrao (Brasilia)")
    void zoneDoPais() {
        assertThat(EphemV2SignalMapper.zoneDoPais("CABO VERDE")).hasToString("Atlantic/Cape_Verde");
        assertThat(EphemV2SignalMapper.zoneDoPais("Guine-Bissau")).hasToString("Africa/Bissau");
        assertThat(EphemV2SignalMapper.zoneDoPais("Guiné-Bissau")).hasToString("Africa/Bissau");
        assertThat(EphemV2SignalMapper.zoneDoPais("Narnia")).hasToString("America/Sao_Paulo");
        assertThat(EphemV2SignalMapper.zoneDoPais(null)).hasToString("America/Sao_Paulo");
    }
}
