package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.HtmlUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NarrativeSignalServiceTest {

    private NarrativeSignalService narrativeSignalService;
    private EventoIntegracao evento;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        narrativeSignalService = new NarrativeSignalService();

        evento = new EventoIntegracao();
        evento.setCreatedAt(Instant.now());
        evento.setUserEmail("user@example.com");
    }

    @Test
    void testBuildHtmlTableWithEmptyMap() {
        Map<String, String> emptyMap = new HashMap<>();
        String result = narrativeSignalService.buildHtmlTable(emptyMap);
        String expected = "";
        assertEquals(expected, result);
    }

    @Test
    void testBuildHtmlTableWithSingleEntry() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("Chave", "Valor");
        String result = narrativeSignalService.buildHtmlTable(dataMap);
        String expected = "<table class=\"table table-bordered o_table\"><tr><td><p>Chave</p></td><td><p>Valor</p></td></tr></table>";
        assertEquals(expected, result);
    }

    @Test
    void testBuildHtmlTableWithMultipleEntries() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("Chave1", "Valor1");
        dataMap.put("Chave2", "Valor2");
        String result = narrativeSignalService.buildHtmlTable(dataMap);
        String expected = "<table class=\"table table-bordered o_table\"><tr><td><p>Chave1</p></td><td><p>Valor1</p></td></tr><tr><td><p>Chave2</p></td><td><p>Valor2</p></td></tr></table>";
        assertEquals(expected, result);
    }

    @Test
    void testBuildHeaderMessage() {
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(zoneId);
        String formattedInstant = formatter.format(evento.getCreatedAt());

        String result = narrativeSignalService.buildHeaderMessage(evento);
        String expected = "<br><p>Reporte pelo App GDS em " + HtmlUtils.htmlEscape(formattedInstant) + " de <a href=\"" + HtmlUtils.htmlEscape(evento.getUserEmail()) + "\">" + HtmlUtils.htmlEscape(evento.getUserEmail()) + "</a></p>";
        assertEquals(expected, result);
    }

    @Test
    void testBuildNarrativeDescriptionWithEmptyMap() {
        String result = narrativeSignalService.buildNarrativeDescription(evento);
        String expected = "<br><p>Reporte pelo App GDS em " + HtmlUtils.htmlEscape(getFormattedInstant(evento.getCreatedAt())) + " de <a href=\"" + HtmlUtils.htmlEscape(evento.getUserEmail()) + "\">" + HtmlUtils.htmlEscape(evento.getUserEmail()) + "</a></p>";
        assertEquals(expected, result);
    }

    @Test
    void testBuildNarrativeDescriptionWithSingleEntry() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("Chave", "Valor");
        evento.setAditionalData(objectMapper.valueToTree(dataMap));

        String result = narrativeSignalService.buildNarrativeDescription(evento);
        String expected = "<br><p>Reporte pelo App GDS em " + HtmlUtils.htmlEscape(getFormattedInstant(evento.getCreatedAt())) + " de <a href=\"" + HtmlUtils.htmlEscape(evento.getUserEmail()) + "\">" + HtmlUtils.htmlEscape(evento.getUserEmail()) + "</a></p><table class=\"table table-bordered o_table\"><tr><td><p>Chave</p></td><td><p>Valor</p></td></tr></table>";
        assertEquals(expected, result);
    }

    @Test
    void testBuildNarrativeDescriptionWithMultipleEntries() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("Chave1", "Valor1");
        dataMap.put("Chave2", "Valor2");
        evento.setAditionalData(objectMapper.valueToTree(dataMap));

        String result = narrativeSignalService.buildNarrativeDescription(evento);
        String expected = "<br><p>Reporte pelo App GDS em " + HtmlUtils.htmlEscape(getFormattedInstant(evento.getCreatedAt())) + " de <a href=\"" + HtmlUtils.htmlEscape(evento.getUserEmail()) + "\">" + HtmlUtils.htmlEscape(evento.getUserEmail()) + "</a></p><table class=\"table table-bordered o_table\"><tr><td><p>Chave1</p></td><td><p>Valor1</p></td></tr><tr><td><p>Chave2</p></td><td><p>Valor2</p></td></tr></table>";
        assertEquals(expected, result);
    }

    // Helper method to format Instant
    private String getFormattedInstant(Instant instant) {
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(zoneId);
        return formatter.format(instant);
    }
}
