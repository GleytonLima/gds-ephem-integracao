package br.unb.sds.gds2ephem.httpcontroller.dto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SignalTest {
    private Signal signal;

    @BeforeEach
    void setUp() {
        Long eventId = 1L;
        Long signalId = 2L;
        Map<String, Object> dados = new HashMap<>();
        this.signal = new Signal(eventId, signalId, dados);
    }

    @Test
    void testTraduzirStatus() {
        signal.getDados().put("status", new Object[] {0, "verification"});
        signal.traduzirStatus();
        Map<String, Object> translatedData = signal.getDados();
        assertEquals("Em verificação", ((Object[]) translatedData.get("status"))[1]);
    }

    @Test
    void testTraduzirStatusInvalid() {
        signal.getDados().put("status", new Object[] {0, "invalid_status"});
        signal.traduzirStatus();
        Map<String, Object> translatedData = signal.getDados();
        assertEquals("invalid_status", ((Object[]) translatedData.get("status"))[1]);
    }

    @Test
    void testTraduzirStatusCaseInsensitive() {
        signal.getDados().put("status", new Object[] {0, "MONITORING"});
        signal.traduzirStatus();
        Map<String, Object> translatedData = signal.getDados();
        assertEquals("Em monitoramento", ((Object[]) translatedData.get("status"))[1]);
    }

    @Test
    void testTraduzirStatusNoTranslation() {
        signal.getDados().put("status", new Object[] {0, "some_status"});
        signal.traduzirStatus();
        Map<String, Object> translatedData = signal.getDados();
        assertEquals("some_status", ((Object[]) translatedData.get("status"))[1]);
    }

    @Test
    void testTraduzirStatusRaw() {
        signal.getDados().put("status", new Object[] {0, "Raw Information"});
        signal.traduzirStatus();
        Map<String, Object> translatedData = signal.getDados();
        assertEquals("Inicial", ((Object[]) translatedData.get("status"))[1]);
    }
}