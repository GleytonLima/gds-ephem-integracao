package br.unb.sds.gds2ephem.ephemv2;

import br.unb.sds.gds2ephem.application.model.exceptions.EphemAuthException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cliente HTTP da API REST v2 do ePHEM.
 * <ul>
 *     <li>401/403 viram {@link EphemAuthException} (sem retry).</li>
 *     <li>422 vira {@link EphemV2ValidationException} (sem retry: nada foi salvo).</li>
 *     <li>429 espera o Retry-After; 5xx e falhas de rede tentam de novo com backoff.</li>
 * </ul>
 */
@Slf4j
public class EphemV2Client {
    interface Sleeper {
        void sleep(Duration duration) throws InterruptedException;
    }

    static final int PAGE_SIZE = 500;

    private final RestTemplate restTemplate;
    private final EphemV2Properties properties;
    private final ObjectMapper objectMapper;
    private final Sleeper sleeper;

    public EphemV2Client(RestTemplate restTemplate, EphemV2Properties properties, ObjectMapper objectMapper) {
        this(restTemplate, properties, objectMapper, duration -> Thread.sleep(duration.toMillis()));
    }

    EphemV2Client(RestTemplate restTemplate, EphemV2Properties properties, ObjectMapper objectMapper, Sleeper sleeper) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.sleeper = sleeper;
    }

    /**
     * POST /signals. Idempotente: reenviar o mesmo external_id devolve o signal existente (200).
     *
     * @return o objeto "data" da resposta (o signal)
     */
    public JsonNode criarSignal(JsonNode payload) {
        return exchange(HttpMethod.POST, "/signals", payload).path("data");
    }

    public Optional<JsonNode> consultarSignal(String externalId) {
        try {
            return Optional.of(exchange(HttpMethod.GET, "/signals/" + externalId, null).path("data"));
        } catch (EphemV2NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * GET /states?country=XX (todas as paginas).
     */
    public List<JsonNode> listarEstados(String countryIso) {
        return listarPaginado("/states?country=" + countryIso);
    }

    /**
     * GET /districts?country=XX (todas as paginas; cada linha traz o state_id).
     */
    public List<JsonNode> listarDistritos(String countryIso) {
        return listarPaginado("/districts?country=" + countryIso);
    }

    private List<JsonNode> listarPaginado(String path) {
        final var rows = new ArrayList<JsonNode>();
        for (int page = 1; page <= 50; page++) {
            final var response = exchange(HttpMethod.GET, path + "&page_size=" + PAGE_SIZE + "&page=" + page, null);
            final var data = response.path("data");
            data.forEach(rows::add);
            final var total = response.path("meta").path("total").asInt(rows.size());
            if (data.size() < PAGE_SIZE || rows.size() >= total) {
                break;
            }
        }
        return rows;
    }


    private JsonNode exchange(HttpMethod method, String path, JsonNode body) {
        final var url = properties.getBaseUrl().replaceAll("/+$", "") + path;
        final var headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        final var entity = new HttpEntity<>(body == null ? null : body.toString(), headers);

        RuntimeException lastError = null;
        for (int attempt = 0; attempt <= properties.getMaxRetries(); attempt++) {
            try {
                final var response = restTemplate.exchange(url, method, entity, String.class);
                return readTree(response.getBody());
            } catch (HttpStatusCodeException e) {
                final var status = e.getStatusCode();
                final var error = readTree(e.getResponseBodyAsString()).path("error");
                if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                    throw new EphemAuthException(String.format("ePHEM API v2 %d: %s (request_id: %s)",
                            status.value(), error.path("message").asText(""), error.path("request_id").asText("")));
                }
                if (status == HttpStatus.UNPROCESSABLE_ENTITY) {
                    throw new EphemV2ValidationException(error);
                }
                if (status == HttpStatus.NOT_FOUND) {
                    throw new EphemV2NotFoundException(error.path("message").asText(path));
                }
                if (status == HttpStatus.TOO_MANY_REQUESTS) {
                    lastError = e;
                    esperar(retryAfter(e.getResponseHeaders()));
                    continue;
                }
                if (status.is5xxServerError()) {
                    lastError = e;
                    log.warn("ePHEM API v2 {} em {} {} (tentativa {}), request_id {}", status.value(), method, path,
                            attempt + 1, error.path("request_id").asText(""));
                    esperar(backoff(attempt));
                    continue;
                }
                throw e;
            } catch (ResourceAccessException e) {
                lastError = e;
                log.warn("falha de rede na ePHEM API v2 em {} {} (tentativa {}): {}", method, path, attempt + 1, e.getMessage());
                esperar(backoff(attempt));
            }
        }
        throw new IllegalStateException(String.format("ePHEM API v2: desistindo de %s %s apos %d tentativas",
                method, path, properties.getMaxRetries() + 1), lastError);
    }

    private Duration retryAfter(HttpHeaders headers) {
        final var value = Optional.ofNullable(headers).map(h -> h.getFirst(HttpHeaders.RETRY_AFTER)).orElse("5");
        Duration duration;
        try {
            duration = Duration.ofSeconds(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            duration = Duration.ofSeconds(5);
        }
        return duration.compareTo(properties.getMaxRetryAfter()) > 0 ? properties.getMaxRetryAfter() : duration;
    }

    private static Duration backoff(int attempt) {
        return Duration.ofSeconds(1L << Math.min(attempt, 4));
    }

    private void esperar(Duration duration) {
        try {
            sleeper.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrompido aguardando a ePHEM API v2", e);
        }
    }

    private JsonNode readTree(String body) {
        try {
            return objectMapper.readTree(body == null || body.isBlank() ? "{}" : body);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }

    public static class EphemV2NotFoundException extends RuntimeException {
        public EphemV2NotFoundException(String message) {
            super(message);
        }
    }
}
