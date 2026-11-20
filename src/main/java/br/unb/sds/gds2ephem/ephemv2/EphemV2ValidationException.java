package br.unb.sds.gds2ephem.ephemv2;

import br.unb.sds.gds2ephem.application.model.exceptions.EventoIntegracaoValidacaoException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * 422 da API v2: nada foi salvo e reenviar o mesmo corpo falha do mesmo jeito.
 */
@Getter
public class EphemV2ValidationException extends EventoIntegracaoValidacaoException {
    private final transient JsonNode error;

    public EphemV2ValidationException(JsonNode error) {
        super(buildMessage(error));
        this.error = error;
    }

    /**
     * @return o primeiro campo citado em error.details (ex.: "geo.district"), se houver
     */
    public Optional<String> firstFieldWithIssue() {
        final var details = error.path("details");
        if (!details.isArray()) {
            return Optional.empty();
        }
        return StreamSupport.stream(details.spliterator(), false)
                .map(detail -> detail.path("field").asText(""))
                .filter(field -> !field.isEmpty())
                .findFirst();
    }

    private static String buildMessage(JsonNode error) {
        final var message = error.path("message").asText("validation error");
        final var requestId = error.path("request_id").asText("");
        return requestId.isEmpty() ? message : String.format("%s (request_id: %s)", message, requestId);
    }
}
