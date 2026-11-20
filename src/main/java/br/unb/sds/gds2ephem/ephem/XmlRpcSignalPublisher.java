package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.SignalPublisher;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.exceptions.EventoIntegracaoValidacaoException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Caminho legado: mapeia o evento pelo template (inputEphemMap), valida com o JSON Schema do template
 * e cria o signal via XML-RPC.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ephem.v2.enabled", havingValue = "false", matchIfMissing = true)
public class XmlRpcSignalPublisher implements SignalPublisher {
    private final EphemPort ephemPort;
    private final NarrativeSignalService narrativeSignalService;
    private final EphemMapper ephemMapper;

    @Override
    public Long publicar(EventoIntegracao eventoIntegracao) {
        final var objectMapper = new ObjectMapper();
        final var dadosMapeados = ephemMapper.mapearData(eventoIntegracao);

        validarDados(eventoIntegracao.getEventoIntegracaoTemplate().getDefinition(), dadosMapeados);
        final var dadosRequest = objectMapper.convertValue(dadosMapeados, new TypeReference<Map<String, Object>>() {
        });
        dadosRequest.put("description", narrativeSignalService.buildNarrativeDescription(eventoIntegracao));

        return ephemPort.criarSignal(eventoIntegracao, dadosRequest);
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
