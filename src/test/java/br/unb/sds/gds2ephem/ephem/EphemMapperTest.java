package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EphemMapperTest {
    @Mock
    private EphemPort ephemPort;
    private Clock clock;

    private EphemMapper ephemMapper;

    @BeforeEach
    void setUp() {
        String instantExpected = "2023-12-22T10:15:30Z";
        Clock clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"));
        ephemMapper = new EphemMapper(ephemPort, clock);
    }

    @Test
    @DisplayName("Deve mapear com sucesso")
    void mapearData() throws IOException {
        final var inputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/input_ephem_map.json");
        final var inputEventoIntegracaoStream = EphemMapperTest.class.getResourceAsStream("/files/input_evento_integracao_data.json");
        final var outputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/output_ephem_map.json");

        final var objectMapper = new ObjectMapper();

        final var inputEphemMap = objectMapper.readTree(inputEphemMapStream);
        final var inputEventoIntegracaoData = objectMapper.readTree(inputEventoIntegracaoStream);
        final var outputEphemMap = objectMapper.readTree(outputEphemMapStream);

        final var eventoIntegracaoTemplate = new EventoIntegracaoTemplate();
        eventoIntegracaoTemplate.setInputEphemMap(inputEphemMap);

        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setData(inputEventoIntegracaoData);
        eventoIntegracao.setEventoIntegracaoTemplate(eventoIntegracaoTemplate);

        final var dataOutput = ephemMapper.mapearData(eventoIntegracao);

        assertEquals(outputEphemMap, dataOutput);
    }

    @Test
    @DisplayName("Deve mapear com sucesso, cenario mais completo")
    void mapearData2() throws IOException {
        final var inputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/input_ephem_map_completo.json");
        final var inputEventoIntegracaoStream = EphemMapperTest.class.getResourceAsStream("/files/input_evento_integracao_data.json");
        final var outputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/output_ephem_map_completo.json");

        final var objectMapper = new ObjectMapper();

        final var inputEphemMap = objectMapper.readTree(inputEphemMapStream);
        final var inputEventoIntegracaoData = objectMapper.readTree(inputEventoIntegracaoStream);
        final var outputEphemMap = objectMapper.readTree(outputEphemMapStream);

        final var eventoIntegracaoTemplate = new EventoIntegracaoTemplate();
        eventoIntegracaoTemplate.setInputEphemMap(inputEphemMap);

        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setData(inputEventoIntegracaoData);
        eventoIntegracao.setEventoIntegracaoTemplate(eventoIntegracaoTemplate);

        final var dataOutput = ephemMapper.mapearData(eventoIntegracao);

        assertEquals(outputEphemMap, dataOutput);
    }

    @Test
    @DisplayName("Deve mapear com sucesso, cenario incompleto")
    void mapearData3() throws IOException {
        final var inputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/input_ephem_map.json");
        final var inputEventoIntegracaoStream = EphemMapperTest.class.getResourceAsStream("/files/input_evento_integracao_sem_geo.json");
        final var outputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/output_ephem_map_sem_geo.json");

        final var objectMapper = new ObjectMapper();

        final var inputEphemMap = objectMapper.readTree(inputEphemMapStream);
        final var inputEventoIntegracaoData = objectMapper.readTree(inputEventoIntegracaoStream);
        final var outputEphemMap = objectMapper.readTree(outputEphemMapStream);

        final var eventoIntegracaoTemplate = new EventoIntegracaoTemplate();
        eventoIntegracaoTemplate.setInputEphemMap(inputEphemMap);

        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setData(inputEventoIntegracaoData);
        eventoIntegracao.setEventoIntegracaoTemplate(eventoIntegracaoTemplate);

        final var dataOutput = ephemMapper.mapearData(eventoIntegracao);

        assertEquals(outputEphemMap, dataOutput);
    }

    @Test
    @DisplayName("Deve mapear com sucesso, cenario incompleto com model array")
    void mapearData4() throws IOException {
        final var inputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/input_ephem_map_model_array.json");
        final var inputEventoIntegracaoStream = EphemMapperTest.class.getResourceAsStream("/files/input_evento_integracao_sem_geo.json");
        final var outputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/output_ephem_map_model_array.json");

        final var objectMapper = new ObjectMapper();

        final var inputEphemMap = objectMapper.readTree(inputEphemMapStream);
        final var inputEventoIntegracaoData = objectMapper.readTree(inputEventoIntegracaoStream);
        final var outputEphemMap = objectMapper.readTree(outputEphemMapStream);

        final var eventoIntegracaoTemplate = new EventoIntegracaoTemplate();
        eventoIntegracaoTemplate.setInputEphemMap(inputEphemMap);

        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setData(inputEventoIntegracaoData);
        eventoIntegracao.setEventoIntegracaoTemplate(eventoIntegracaoTemplate);

        final var dataOutput = ephemMapper.mapearData(eventoIntegracao);

        assertEquals(outputEphemMap, dataOutput);
    }


    @Test
    @DisplayName("Deve mapear com sucesso, cenario incompleto com model array")
    void mapearData5() throws IOException {
        final var inputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/input_ephem_map_model_array.json");
        final var inputEventoIntegracaoStream = EphemMapperTest.class.getResourceAsStream("/files/input_evento_integracao_sem_geo_02.json");
        final var outputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/output_ephem_map_model_array_02.json");

        final var objectMapper = new ObjectMapper();

        final var inputEphemMap = objectMapper.readTree(inputEphemMapStream);
        final var inputEventoIntegracaoData = objectMapper.readTree(inputEventoIntegracaoStream);
        final var outputEphemMap = objectMapper.readTree(outputEphemMapStream);

        final var eventoIntegracaoTemplate = new EventoIntegracaoTemplate();
        eventoIntegracaoTemplate.setInputEphemMap(inputEphemMap);

        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setData(inputEventoIntegracaoData);
        eventoIntegracao.setEventoIntegracaoTemplate(eventoIntegracaoTemplate);

        final var dataOutput = ephemMapper.mapearData(eventoIntegracao);

        assertEquals(outputEphemMap, dataOutput);
    }
}