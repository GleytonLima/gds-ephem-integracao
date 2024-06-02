package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.ConfiguracaoSistemaRepository;
import br.unb.sds.gds2ephem.application.model.SignalSource;
import br.unb.sds.gds2ephem.configs.XmlRpcClientBeans;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import static br.unb.sds.gds2ephem.EventoIntegracaoObjectMother.createIntegrationEvent;
import static br.unb.sds.gds2ephem.ephem.EphemParameters.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class EphemAdapterIntegrationTest {
    private EphemAdapter ephemAdapter;
    @Mock
    private ConfiguracaoSistemaRepository configuracaoSistemaRepository;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        final var client = new XmlRpcClientBeans();
        ReflectionTestUtils.setField(client, "url", System.getenv("ODOO_URL"));
        ephemAdapter = new EphemAdapter(client.gerarClientObject(), client.gerarClientCommon(), configuracaoSistemaRepository);
    }

    @SneakyThrows
    @Test
    @Disabled
    void criarSignal() {
        ReflectionTestUtils.setField(ephemAdapter, "uid", 2);
        ReflectionTestUtils.setField(ephemAdapter, "db", System.getenv("ODOO_DB"));
        ReflectionTestUtils.setField(ephemAdapter, "odooApiKey", System.getenv("ODOO_APIKEY"));
        final var dados = new HashMap<String, Object>() {{
            put("general_hazard_id", false);
            put("confidentiality", "pheoc");
            put("specific_hazard_id", 2);
            put("state_id", false);
            put("signal_type", "opening");
            put("description", "<br><p>Reporte pelo App GDS em 24/07/2023&nbsp; as 21H de <a href=\"https://teste@gmail.com\">teste@gmail.com</a></p><table class=\"table table-bordered o_table\"><tbody><tr><td><p>Tipo de Notificação</p></td><td><p>Coletiva</p></td></tr><tr><td><p>Tipo de Ocorrência</p></td><td><p>Em Humanos</p></td></tr><tr><td><p>Quantos Envolvidos</p></td><td><p>Mais de 5</p></td></tr></tbody></table><p><br></p><p><br></p>");
            put("report_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            put("incident_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        }};
        final var id = ephemAdapter.criarSignal(createIntegrationEvent(), dados);
    }

    @SneakyThrows
    @Test
    @Disabled
    void addSource() {
        ReflectionTestUtils.setField(ephemAdapter, "uid", 2);
        ReflectionTestUtils.setField(ephemAdapter, "db", "unbhom");
        ReflectionTestUtils.setField(ephemAdapter, "odooApiKey", "e530771d32bc1a11ce0cdc8fc10b291dca8f5429");

        final var signalSource = new SignalSource();
        signalSource.setSignalId(33L);
        signalSource.setSourceName("Teste");
        signalSource.setSourcePhone("999999999");
        signalSource.setSourceType(1L);

        final var id = ephemAdapter.addSource(signalSource);
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
        ReflectionTestUtils.setField(ephemAdapter, "db", System.getenv("ODOO_DB"));
        ReflectionTestUtils.setField(ephemAdapter, "odooApiKey", System.getenv("ODOO_APIKEY"));

        final var modelos = ephemAdapter.listarModelos("eoc_", "message");
        final var modelosCampos = ephemAdapter.listarCamposModelo("eoc.signal", "confidentiality");
        final var atributos = ephemAdapter.listarAtributosPorNomeModelo("eoc.signal");
        final var opcoesSelecaoString = ((HashMap) modelosCampos.get(0)).get("selection");
        final var opcoesSelecao = EphemUtils.converterPythonStringTupleEmList((String) opcoesSelecaoString);

        final var parametersTags = EphemParameters.builder()
                .nomeModelo(EOC_SIGNAL_TAGS_MODEL_NAME)
                .fields(List.of("name"))
                .build();
        final var registrosTags = ephemAdapter.listarRegistros(parametersTags);

        final var parametersSignals = EphemParameters.builder()
                .nomeModelo(SIGNAL_MODEL_NAME)
                .fields(SIGNAL_DEFAULT_PARAMETERS)
                .build();
        final var registrosSinals = ephemAdapter.listarRegistros(parametersSignals);

        final var parametersMessage = EphemParameters.builder()
                .nomeModelo(MAIL_MESSAGE_MODEL_NAME)
                .build();
        final var registrosMessage = ephemAdapter.listarRegistros(parametersMessage);

        assertNotNull(modelos);
        assertNotNull(modelosCampos);
        assertNotNull(atributos);
        assertNotNull(opcoesSelecao);
        assertNotNull(registrosTags);
        assertNotNull(registrosSinals);
        assertNotNull(registrosMessage);
    }
}