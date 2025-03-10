package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoStatus;
import br.unb.sds.gds2ephem.application.model.exceptions.EventoIntegracaoValidacaoException;
import br.unb.sds.gds2ephem.infrastructure.NotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final EphemPort ephemPort;
    private final NotificationService notificationService;

    private final NarrativeSignalService narrativeSignalService;
    private final EphemMapper ephemMapper;

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
                        tratarCaracteresEspeciaisEventoIntegracao(eventoIntegracao);
                        final var dadosMapeados = ephemMapper.mapearData(eventoIntegracao);

                        validarDados(eventoIntegracao.getEventoIntegracaoTemplate().getDefinition(), dadosMapeados);
                        final var dadosRequest = objectMapper.convertValue(dadosMapeados, new TypeReference<Map<String, Object>>() {
                        });
                        dadosRequest.put("description", narrativeSignalService.buildNarrativeDescription(eventoIntegracao));

                        final var signalId = ephemPort.criarSignal(eventoIntegracao, dadosRequest);

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

    public void validarDados(JsonNode definition, JsonNode data) {
        final var factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        final var jsonSchema = factory.getSchema(definition);

        final var errosSchema = jsonSchema.validate(data);

        final var listaErros = errosSchema.stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toList());

        if (!listaErros.isEmpty()) {
            log.error("Erro ao processar os dados: {}", listaErros);
            throw new EventoIntegracaoValidacaoException(listaErros.toString());
        }
    }
}
