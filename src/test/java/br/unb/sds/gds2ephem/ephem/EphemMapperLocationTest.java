package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Caminho XML-RPC: pais, estado e municipio sao procurados no ePHEM por nome (=ilike) e enviados como
 * comandos many2many do Odoo: [[6, 0, [id]]].
 */
@ExtendWith(MockitoExtension.class)
class EphemMapperLocationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private EphemPort ephemPort;
    private EphemMapper mapper;
    /**
     * Respostas do search_read por modelo; modelo ausente = nenhum registro encontrado.
     */
    private final Map<String, Integer> idsPorModelo = new HashMap<>();

    @BeforeEach
    void setUp() {
        mapper = new EphemMapper(ephemPort, Clock.fixed(Instant.parse("2026-11-18T12:00:00Z"), ZoneId.of("UTC")));
        lenient().when(ephemPort.listarRegistros(any())).thenAnswer(inv -> {
            final EphemParameters parametros = inv.getArgument(0);
            final var id = idsPorModelo.get(parametros.getNomeModelo());
            final var resultado = new ArrayList<Object>();
            if (id != null) {
                final var registro = new HashMap<String, Object>();
                registro.put("id", id);
                resultado.add(registro);
            }
            return resultado;
        });
    }

    @SneakyThrows
    private EventoIntegracao evento(String pais, String data) {
        final var template = new EventoIntegracaoTemplate();
        template.setLocationMap(objectMapper.readTree("{\"country_source_field\": \"evento_pais_ocorrencia\","
                + " \"state_source_field\": \"evento_estado_ocorrencia\", \"district_source_field\": \"evento_municipio_ocorrencia\"}"));
        final var evento = new EventoIntegracao();
        evento.setUserCountry(pais);
        evento.setEventoIntegracaoTemplate(template);
        evento.setData(objectMapper.readTree(data));
        return evento;
    }

    private ObjectNode mapear(EventoIntegracao evento) {
        final var dados = objectMapper.createObjectNode();
        mapper.mapLocationFields(dados, evento);
        return dados;
    }

    @Test
    @DisplayName("Pais, estado e municipio encontrados viram comandos [[6, 0, [id]]]")
    void localCompleto() {
        idsPorModelo.put("res.country", 31);
        idsPorModelo.put("res.country.state", 501);
        idsPorModelo.put("res.country.state.district", 9001);

        final var dados = mapear(evento("Brasil", "{\"evento_estado_ocorrencia\": \"Amazonas\", \"evento_municipio_ocorrencia\": \"Manaus\"}"));

        assertThat(dados.toString()).isEqualTo("{\"source_country_id\":31,\"country_ids\":[[6,0,[31]]],"
                + "\"state_ids\":[[6,0,[501]]],\"district_ids\":[[6,0,[9001]]]}");
    }

    @Test
    @DisplayName("Estado e procurado dentro do pais, e municipio dentro do estado")
    void filtrosHierarquicos() {
        idsPorModelo.put("res.country", 31);
        idsPorModelo.put("res.country.state", 501);
        idsPorModelo.put("res.country.state.district", 9001);

        mapear(evento("Brasil", "{\"evento_estado_ocorrencia\": \"Amazonas\", \"evento_municipio_ocorrencia\": \"Manaus\"}"));

        final var captor = ArgumentCaptor.forClass(EphemParameters.class);
        verify(ephemPort, times(3)).listarRegistros(captor.capture());
        final var chamadas = captor.getAllValues();
        // "Brasil" nao esta na tabela de traducao: vai como veio (o =ilike ignora maiusculas)
        assertThat(chamadas.get(0).getFiltros()).isEqualTo(List.of(List.of(List.of("name", "=ilike", "Brasil"))));
        assertThat(chamadas.get(1).getFiltros()).isEqualTo(List.of(List.of(
                List.of("name", "=ilike", "Amazonas"), List.of("country_id", "=", 31))));
        assertThat(chamadas.get(2).getFiltros()).isEqualTo(List.of(List.of(
                List.of("name", "=ilike", "Manaus"), List.of("state_id", "=", 501))));
        // o idioma fica a cargo do adapter (odoo.lang), que conhece os idiomas ativos no servidor
        chamadas.forEach(c -> assertThat(c.getContextLang()).isNull());
    }

    @Test
    @DisplayName("Nome do pais em ingles e traduzido antes da busca (Cape Verde -> cabo verde)")
    void traducaoPais() {
        mapear(evento("Cape Verde", "{}"));

        final var captor = ArgumentCaptor.forClass(EphemParameters.class);
        verify(ephemPort).listarRegistros(captor.capture());
        assertThat(captor.getValue().getFiltros()).isEqualTo(List.of(List.of(List.of("name", "=ilike", "cabo verde"))));
    }

    @Test
    @DisplayName("Sem pais do usuario, nada e preenchido e o ePHEM nao e consultado")
    void semPais() {
        assertThat(mapear(evento(null, "{}")).size()).isZero();
        verifyNoInteractions(ephemPort);
    }

    @Test
    @DisplayName("Pais nao encontrado no ePHEM: nada e preenchido")
    void paisNaoEncontrado() {
        assertThat(mapear(evento("Narnia", "{\"evento_estado_ocorrencia\": \"X\"}")).size()).isZero();
    }

    @Test
    @DisplayName("Evento sem estado: so o pais e preenchido")
    void soPais() {
        idsPorModelo.put("res.country", 31);

        assertThat(mapear(evento("Brasil", "{}")).toString())
                .isEqualTo("{\"source_country_id\":31,\"country_ids\":[[6,0,[31]]]}");
    }

    @Test
    @DisplayName("Municipio nao encontrado: pais e estado sao mantidos")
    void municipioNaoEncontrado() {
        idsPorModelo.put("res.country", 31);
        idsPorModelo.put("res.country.state", 501);

        final var dados = mapear(evento("Brasil", "{\"evento_estado_ocorrencia\": \"Amazonas\", \"evento_municipio_ocorrencia\": \"Manuas\"}"));

        assertThat(dados.has("state_ids")).isTrue();
        assertThat(dados.has("district_ids")).isFalse();
    }

    @Test
    @DisplayName("Falha na consulta ao ePHEM nao derruba o mapeamento: o nivel fica de fora")
    void falhaNaConsulta() {
        idsPorModelo.put("res.country", 31);
        doAnswer(inv -> {
            final EphemParameters p = inv.getArgument(0);
            if (p.getNomeModelo().equals("res.country.state")) {
                throw new IllegalStateException("timeout");
            }
            final var registro = new HashMap<String, Object>();
            registro.put("id", 31);
            return new ArrayList<Object>(List.of(registro));
        }).when(ephemPort).listarRegistros(any());

        final var dados = mapear(evento("Brasil", "{\"evento_estado_ocorrencia\": \"Amazonas\"}"));

        assertThat(dados.has("country_ids")).isTrue();
        assertThat(dados.has("state_ids")).isFalse();
    }
}
