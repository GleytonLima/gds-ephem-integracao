package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.application.SignalPublisher;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoStatus;
import br.unb.sds.gds2ephem.infrastructure.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static br.unb.sds.gds2ephem.ephem.EmojiToPortugues.tratarCaracteresEspeciaisEventoIntegracao;

@Component
@Slf4j
@RequiredArgsConstructor
public class EphemScheduler {
    public static final int MAX_SIZE_MESSAGE = 255;
    private static final int QUANTIDADE_MAXIMA_ITERACOES = 5;
    public static final String STATUS_MESSAGE_ERRO_INESPERADO = "Erro inesperado";
    private final TransactionTemplate transactionTemplate;

    private final EventoIntegracaoRepository eventoIntegracaoRepository;
    private final NotificationService notificationService;
    private final SignalPublisher signalPublisher;

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
                        tratarCaracteresEspeciaisEventoIntegracao(eventoIntegracao);

                        final Long signalId = signalPublisher.publicar(eventoIntegracao);

                        eventoIntegracao.setSignalId(signalId);
                        eventoIntegracao.setStatus(EventoIntegracaoStatus.PROCESSADO.name());
                        eventoIntegracao.setStatusMessage(String.format("signal criado com suceso com id: %d", signalId));
                        eventoIntegracaoRepository.save(eventoIntegracao);
                    } catch (Exception e) {
                        log.error("falha na criacao do evento", e);

                        // Notificar erro
                        String assunto = "GDS/Ephem - Integrador - Erro no Processamento de Evento";
                        String mensagem = String.format("Ocorreu um erro ao processar o evento de ID %s",
                                eventoIntegracao.getId());
                        notificationService.notificarErro(assunto, mensagem, e);

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
