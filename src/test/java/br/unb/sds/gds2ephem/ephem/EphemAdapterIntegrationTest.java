package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.configs.XmlRpcClientBeans;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class EphemAdapterIntegrationTest {
    private EphemAdapter ephemAdapter;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        final var client = new XmlRpcClientBeans();
        ReflectionTestUtils.setField(client, "url", "https://homologacao4.sds.unb.br");
        ephemAdapter = new EphemAdapter(client.gerarClientObject(), client.gerarClientCommon());
    }

    @SneakyThrows
    @Test
    @Disabled
    void criarSignal() {
        ReflectionTestUtils.setField(ephemAdapter, "uid", 2);
        ReflectionTestUtils.setField(ephemAdapter, "db", "test");
        ReflectionTestUtils.setField(ephemAdapter, "odooApiKey", "e912911d5a66b85dee0b6f04ab054cd1a2ed898f");
        final var dados = new HashMap<String, Object>() {{
            put("general_hazard_id", false);
            put("confidentiality", "pheoc");
            put("specific_hazard_id", 2);
            put("state_id", false);
            put("signal_type", "opening");
            put("description", "<br><p>Reporte pelo App GDS em 24/07/2023&nbsp; as 21H de <a href=\"https://teste@gmail.com\">teste.carmine@gmail.com</a></p><table class=\"table table-bordered o_table\"><tbody><tr><td><p>Tipo de Notificação</p></td><td><p>Coletiva</p></td></tr><tr><td><p>Tipo de Ocorrência</p></td><td><p>Em Humanos</p></td></tr><tr><td><p>Quantos Envolvidos</p></td><td><p>Mais de 5</p></td></tr></tbody></table><p><br></p><p><br></p>");
            put("report_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            put("incident_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        }};
        final var id = ephemAdapter.criarSignal(dados);
    }

    @SneakyThrows
    @Test
    @Disabled
    void deveRecuperarInstanciaOdoo() {
        ephemAdapter.recuperarVersaoInstanciaOdoo();
    }

    @SneakyThrows
    @Test
    @Disabled
    void deveAutenticar() {
        ephemAdapter.autenticar();
        assertNotNull(ephemAdapter.getUid());
    }

    @SneakyThrows
    @Test
    @Disabled
    void deveExecutarTodasAsConsultasComSucesso() {
        ReflectionTestUtils.setField(ephemAdapter, "uid", 2);
        ReflectionTestUtils.setField(ephemAdapter, "db", "test");
        ReflectionTestUtils.setField(ephemAdapter, "odooApiKey", "e912911d5a66b85dee0b6f04ab054cd1a2ed898f");
        final var modelos = ephemAdapter.listarModelos("eoc_", "message");
        final var modelosCampos = ephemAdapter.listarCamposModelo("eoc.signal", "confidentiality");
        final var atributos = ephemAdapter.listarAtributosPorNomeModelo("eoc.signal");
        final var opcoesSelecaoString = ((HashMap) modelosCampos.get(0)).get("selection");
        final var opcoesSelecao = EphemUtils.converterPythonStringTupleEmList((String) opcoesSelecaoString);
        final var registrosTags = ephemAdapter.listarRegistrosPorNomeModelo("eoc.signal.tags", asList("name"));
        final var registrosSinals = ephemAdapter.listarRegistrosPorNomeModelo("eoc.signal", asList("id", "description", "confidentiality", "tag_ids", "general_hazard_id", "specific_hazard_id", "state_id", "signal_type", "report_date", "incident_date", "name", "message_ids"));
        final var registrosMessage = ephemAdapter.listarRegistrosPorNomeModelo("mail.message", Collections.emptyList());

        assertNotNull(modelos);
        assertNotNull(modelosCampos);
        assertNotNull(atributos);
        assertNotNull(opcoesSelecao);
        assertNotNull(registrosTags);
        assertNotNull(registrosSinals);
        assertNotNull(registrosMessage);
    }
}