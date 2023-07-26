package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class EphemScheduler {
    public static final int MAX_SIZE_MESSAGE = 255;
    private static final int QUANTIDADE_MAXIMA_ITERACOES = 5;
    public static final String STATUS_MESSAGE_ERRO_INESPERADO = "Erro inesperado";
    private final TransactionTemplate transactionTemplate;

    private final EventoIntegracaoRepository eventoIntegracaoRepository;
    private final EphemPort ephemPort;

    private final NarrativeSignalService narrativeSignalService;

    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    public void execute() {
        log.info("iniciando processador eventos");
        boolean pending = true;
        int countDefensivaWhileInfinito = 0;
        while (pending) {
            countDefensivaWhileInfinito++;
            if (countDefensivaWhileInfinito > QUANTIDADE_MAXIMA_ITERACOES) {
                break;
            }
            pending = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
                final var eventos = eventoIntegracaoRepository.findTop10ByStatusOrderByCreatedAtAsc(EventoIntegracaoStatus.CRIADO.name());
                if (eventos.isEmpty()) {
                    log.info("sem eventos a serem processados");
                    return false;
                }
                for (EventoIntegracao eventoIntegracao : eventos) {
                    try {
                        log.info("processando evento {}", eventoIntegracao.getId());
                        final var objectMapper = new ObjectMapper();
                        final var dadosRequest = objectMapper.convertValue(eventoIntegracao.getData(), new TypeReference<Map<String, Object>>() {
                        });
                        dadosRequest.put("description", narrativeSignalService.buildNarrativeDescription(eventoIntegracao));

                        final var signalId = ephemPort.criarSignal(dadosRequest);

                        eventoIntegracao.setSignalId(signalId);
                        eventoIntegracao.setStatus(EventoIntegracaoStatus.PROCESSADO.name());
                        eventoIntegracao.setStatusMessage(String.format("signal criado com suceso com id: %d", signalId));
                        eventoIntegracaoRepository.save(eventoIntegracao);
                    } catch (Exception e) {
                        log.error("falha na criacao do evento", e);
                        eventoIntegracao.setStatus(EventoIntegracaoStatus.ERRO.name());
                        final var errorMessage = Optional.ofNullable(e.getMessage()).orElse(STATUS_MESSAGE_ERRO_INESPERADO);
                        eventoIntegracao.setStatusMessage(errorMessage.substring(0, Math.min(errorMessage.length(), MAX_SIZE_MESSAGE)));
                        eventoIntegracaoRepository.save(eventoIntegracao);
                    }
                }
                return true;
            }));
        }
    }
}
