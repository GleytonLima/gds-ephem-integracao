package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoStatus;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EphemSchedulerTest {
    private EphemScheduler scheduler;

    @Mock
    private TransactionStatus _transactionStatus;
    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private EventoIntegracaoRepository eventoIntegracaoRepository;

    @Mock
    private EphemPort ephemPort;

    @Mock
    private EphemMapper ephemMapper;

    @Mock
    private NarrativeSignalService narrativeSignalService;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        scheduler = new EphemScheduler(transactionTemplate, eventoIntegracaoRepository, ephemPort, narrativeSignalService, ephemMapper);
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve executar com sucesso dado que nao ha eventos no banco de dados")
    void execute_NoPendingEventos() {
        when(eventoIntegracaoRepository.findTop10ByStatusOrderByCreatedAtAsc(EventoIntegracaoStatus.CRIADO.name()))
                .thenReturn(new ArrayList<>());
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> invocation.<TransactionCallback<Boolean>>getArgument(0).doInTransaction(_transactionStatus));

        scheduler.execute();

        verify(transactionTemplate, times(1)).execute(any());
        verify(eventoIntegracaoRepository, times(1))
                .findTop10ByStatusOrderByCreatedAtAsc(EventoIntegracaoStatus.CRIADO.name());
        verify(ephemPort, never()).criarSignal(any(), any());
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve executar duas vezes dado que ha 2 eventos no banco de dados")
    void execute_WithPendingEventos() {
        final var outputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/output_ephem_map.json");
        final var objectMapper = new ObjectMapper();
        final var outputEphemMap = objectMapper.readTree(outputEphemMapStream);
        when(ephemMapper.mapearData(any())).thenReturn(outputEphemMap);
        final var eventos = new ArrayList<EventoIntegracao>();
        eventos.add(createEventoIntegracao(EventoIntegracaoStatus.CRIADO));
        eventos.add(createEventoIntegracao(EventoIntegracaoStatus.CRIADO));

        when(eventoIntegracaoRepository.findTop10ByStatusOrderByCreatedAtAsc(EventoIntegracaoStatus.CRIADO.name()))
                .thenReturn(eventos).thenReturn(Collections.emptyList());
        when(ephemPort.criarSignal(any(), any())).thenReturn(123L);
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> invocation.<TransactionCallback<Boolean>>getArgument(0).doInTransaction(_transactionStatus));

        scheduler.execute();

        verify(transactionTemplate, times(2)).execute(any());
        verify(eventoIntegracaoRepository, times(2))
                .findTop10ByStatusOrderByCreatedAtAsc(EventoIntegracaoStatus.CRIADO.name());
        verify(ephemPort, times(2)).criarSignal(any(), any());
        verify(eventoIntegracaoRepository, times(2)).save(any(EventoIntegracao.class));
    }

    @SneakyThrows
    @Test
    @DisplayName("Deve executar duas vezes dado que ha 2 eventos no banco de dados, ainda que haja erro no ephem")
    void deveCapturarExcecaoEContinuarLoop() {
        final var outputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/output_ephem_map.json");
        final var objectMapper = new ObjectMapper();
        final var outputEphemMap = objectMapper.readTree(outputEphemMapStream);
        when(ephemMapper.mapearData(any())).thenReturn(outputEphemMap);
        final var eventos = new ArrayList<EventoIntegracao>();
        eventos.add(createEventoIntegracao(EventoIntegracaoStatus.CRIADO));
        eventos.add(createEventoIntegracao(EventoIntegracaoStatus.CRIADO));

        when(eventoIntegracaoRepository.findTop10ByStatusOrderByCreatedAtAsc(EventoIntegracaoStatus.CRIADO.name()))
                .thenReturn(eventos).thenReturn(Collections.emptyList());
        when(ephemPort.criarSignal(any(), any())).thenThrow(new RuntimeException("Erro Qualquer"));
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> invocation.<TransactionCallback<Boolean>>getArgument(0).doInTransaction(_transactionStatus));

        scheduler.execute();

        verify(transactionTemplate, times(2)).execute(any());
        verify(eventoIntegracaoRepository, times(2))
                .findTop10ByStatusOrderByCreatedAtAsc(EventoIntegracaoStatus.CRIADO.name());
        verify(ephemPort, times(2)).criarSignal(any(), any());
        verify(eventoIntegracaoRepository, times(2)).save(any(EventoIntegracao.class));
    }


    @SneakyThrows
    @Test
    @DisplayName("Deve executar no maximo 5 vezes para impedir um loop infinito")
    void deveImpedirLoopInfinito() {
        final var outputEphemMapStream = EphemMapperTest.class.getResourceAsStream("/files/output_ephem_map.json");
        final var objectMapper = new ObjectMapper();
        final var outputEphemMap = objectMapper.readTree(outputEphemMapStream);
        when(ephemMapper.mapearData(any())).thenReturn(outputEphemMap);
        final var eventos = new ArrayList<EventoIntegracao>();
        eventos.add(createEventoIntegracao(EventoIntegracaoStatus.CRIADO));
        eventos.add(createEventoIntegracao(EventoIntegracaoStatus.CRIADO));

        when(eventoIntegracaoRepository.findTop10ByStatusOrderByCreatedAtAsc(EventoIntegracaoStatus.CRIADO.name()))
                .thenReturn(eventos);
        when(ephemPort.criarSignal(any(), any())).thenReturn(123L);
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> invocation.<TransactionCallback<Boolean>>getArgument(0).doInTransaction(_transactionStatus));

        scheduler.execute();

        verify(transactionTemplate, times(5)).execute(any());
        verify(eventoIntegracaoRepository, times(5))
                .findTop10ByStatusOrderByCreatedAtAsc(EventoIntegracaoStatus.CRIADO.name());
        verify(ephemPort, times(10)).criarSignal(any(), any());
        verify(eventoIntegracaoRepository, times(10)).save(any(EventoIntegracao.class));
    }

    @SneakyThrows
    private EventoIntegracao createEventoIntegracao(EventoIntegracaoStatus status) {
        final var mapper = new ObjectMapper();
        final var eventoIntegracaoTemplate = new EventoIntegracaoTemplate();
        final var inputEphemMapStream = EphemSchedulerTest.class.getResourceAsStream("/files/input_ephem_map.json");
        final var definitionStream = EphemSchedulerTest.class.getResourceAsStream("/files/schema.json");
        eventoIntegracaoTemplate.setInputEphemMap(mapper.readTree(inputEphemMapStream));
        eventoIntegracaoTemplate.setDefinition(mapper.readTree(definitionStream));
        final var eventoIntegracao = new EventoIntegracao();
        final var jsonNodeTarget = mapper.readTree("{\"field_1\": true}");
        eventoIntegracao.setData(jsonNodeTarget);
        eventoIntegracao.setStatus(status.name());
        eventoIntegracao.setEventoIntegracaoTemplate(eventoIntegracaoTemplate);

        return eventoIntegracao;
    }

}