package br.unb.sds.gds2ephem.ephemv2;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.ephem.EphemMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

/**
 * Monta o corpo do POST /api/v2/signals a partir de um evento do GDS.
 * <p>
 * O "geo" sai com o pais em ISO e os nomes de estado e municipio como vieram do app; o
 * {@link EphemV2PlaceResolver} troca os nomes pelos ids antes do envio.
 */
@Slf4j
public class EphemV2SignalMapper {
    public static final int TITLE_MAX_LENGTH = 50;
    static final String HUMAN = "Human";
    static final String ANIMAL = "Animal";
    static final String ENVIRONMENT = "Environment";

    /**
     * Nomes de pais como chegam do app GDS (sem acento, minusculo) para o codigo ISO,
     * que o ePHEM aceita sem depender da lingua em que o nome foi cadastrado.
     */
    static final Map<String, String> COUNTRY_ISO = Map.ofEntries(
            Map.entry("brasil", "BR"),
            Map.entry("brazil", "BR"),
            Map.entry("cabo verde", "CV"),
            Map.entry("cape verde", "CV"),
            Map.entry("portugal", "PT"),
            Map.entry("angola", "AO"),
            Map.entry("mocambique", "MZ"),
            Map.entry("mozambique", "MZ"),
            Map.entry("timor leste", "TL"),
            Map.entry("timor-leste", "TL"),
            Map.entry("east timor", "TL"),
            Map.entry("guine-bissau", "GW"),
            Map.entry("guinea-bissau", "GW"),
            Map.entry("sao tome e principe", "ST"),
            Map.entry("sao tome and principe", "ST"),
            Map.entry("guine-equatorial", "GQ"),
            Map.entry("guine equatorial", "GQ"),
            Map.entry("equatorial guinea", "GQ")
    );

    private static final List<String> ONSET_DATE_TIME_FORMATS = List.of("yyyy-MM-dd'T'HH:mm:ssSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final List<String> ONSET_DATE_FORMATS = List.of("dd-MM-yyyy", "yyyy-MM-dd", "dd/MM/yyyy");

    private final EphemV2Properties properties;
    private final ObjectMapper objectMapper;

    public EphemV2SignalMapper(EphemV2Properties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String externalId(EventoIntegracao evento) {
        return properties.getExternalIdPrefix() + evento.getId();
    }

    public ObjectNode mapear(EventoIntegracao evento, String description) {
        final var data = Optional.ofNullable(evento.getData()).orElse(objectMapper.createObjectNode());
        final var body = objectMapper.createObjectNode();

        body.put("external_id", externalId(evento));
        body.put("title", title(evento, data));
        body.putArray("health_interface").add(healthInterface(data));
        body.put("description", description);
        body.put("signal_type", "primary");
        body.put("confidentiality", properties.getConfidentiality());
        body.put("privacy", properties.getPrivacy());
        if (evento.getCreatedAt() != null) {
            body.put("report_date", iso(evento.getCreatedAt()));
        }
        onsetDate(evento, data).ifPresent(onset -> body.put("onset_date", onset));

        final var geo = geo(evento, data);
        if (geo.size() > 0) {
            body.set("geo", geo);
        }
        body.set("source", source(evento));
        if (isEmail(evento.getUserEmail())) {
            body.put("external_user_email", evento.getUserEmail().trim());
        }
        return body;
    }

    String title(EventoIntegracao evento, JsonNode data) {
        final var raw = data.path(properties.getTitleField()).asText("").trim();
        final var title = raw.isEmpty() ? "Reporte GDS #" + evento.getId() : raw;
        if (title.length() <= TITLE_MAX_LENGTH) {
            return title;
        }
        return title.substring(0, TITLE_MAX_LENGTH - 3).trim() + "...";
    }

    String healthInterface(JsonNode data) {
        final var afetados = normalizar(data.path(properties.getHealthInterfaceField()).asText(""));
        if (afetados.contains("animal") || afetados.contains("animais")) {
            return ANIMAL;
        }
        if (afetados.contains("ambient") || afetados.contains("environment")) {
            return ENVIRONMENT;
        }
        return HUMAN;
    }

    ObjectNode geo(EventoIntegracao evento, JsonNode data) {
        final var geo = objectMapper.createObjectNode();
        final var locationMap = Optional.ofNullable(evento.getEventoIntegracaoTemplate())
                .map(t -> t.getLocationMap())
                .orElse(null);

        paisDoEvento(evento, data).ifPresent(c -> geo.put("country", COUNTRY_ISO.getOrDefault(normalizar(c), c.trim())));
        texto(data, locationMap, "state_source_field").ifPresent(s -> geo.put("state", s));
        texto(data, locationMap, "district_source_field").ifPresent(d -> geo.put("district", d));
        return geo;
    }

    ObjectNode source(EventoIntegracao evento) {
        final var source = objectMapper.createObjectNode();
        if (!isBlank(evento.getUserPhone())) {
            source.put("phone", evento.getUserPhone().trim());
        }
        if (isEmail(evento.getUserEmail())) {
            source.put("email", evento.getUserEmail().trim());
        }
        if (source.size() == 0) {
            source.put("address", properties.getSourceAddress());
        }
        return source;
    }

    Optional<String> onsetDate(EventoIntegracao evento, JsonNode data) {
        final var raw = data.path(properties.getOnsetDateField()).asText("").trim();
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(iso(OffsetDateTime.parse(raw).toInstant()));
        } catch (DateTimeParseException ignored) {
            // nao e ISO 8601 com offset; tenta os formatos usados pelo app
        }
        for (String format : ONSET_DATE_TIME_FORMATS) {
            try {
                return Optional.of(iso(OffsetDateTime.parse(raw, DateTimeFormatter.ofPattern(format)).toInstant()));
            } catch (DateTimeParseException ignored) {
                // tenta o proximo formato
            }
        }
        for (String format : ONSET_DATE_FORMATS) {
            try {
                final var date = LocalDate.parse(raw, DateTimeFormatter.ofPattern(format));
                return Optional.of(iso(date.atStartOfDay(zoneDoPais(paisDoEvento(evento, data).orElse(null))).toInstant()));
            } catch (DateTimeParseException ignored) {
                // tenta o proximo formato
            }
        }
        log.warn("evento {}: data de ocorrencia '{}' em formato desconhecido; onset_date nao sera enviado", evento.getId(), raw);
        return Optional.empty();
    }

    /**
     * Pais do usuario; sem ele (ex.: usuario criado pelo admin), o pais informado no formulario.
     * Decide tanto o geo.country quanto o fuso das datas sem hora.
     */
    static Optional<String> paisDoEvento(EventoIntegracao evento, JsonNode data) {
        final var locationMap = Optional.ofNullable(evento.getEventoIntegracaoTemplate())
                .map(t -> t.getLocationMap())
                .orElse(null);
        return Optional.ofNullable(evento.getUserCountry())
                .filter(c -> !c.isBlank())
                .or(() -> texto(data, locationMap, "country_source_field"));
    }

    static ZoneId zoneDoPais(String pais) {
        final var chave = Optional.ofNullable(pais).orElse("").toLowerCase(Locale.ROOT).trim();
        var zone = EphemMapper.COUNTRY_ZONE_ID_MAP.get(chave);
        if (isNull(zone)) {
            // as chaves do mapa misturam nomes com e sem acento ("guiné-bissau", "sao tome and principe")
            final var semAcento = normalizar(chave);
            zone = EphemMapper.COUNTRY_ZONE_ID_MAP.entrySet().stream()
                    .filter(e -> normalizar(e.getKey()).equals(semAcento))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return ZoneId.of(isNull(zone) ? EphemMapper.DEFAULT_ZONE_ID : zone);
    }

    static String iso(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant.truncatedTo(ChronoUnit.SECONDS));
    }

    private static Optional<String> texto(JsonNode data, JsonNode locationMap, String chaveLocationMap) {
        if (isNull(locationMap) || !locationMap.hasNonNull(chaveLocationMap)) {
            return Optional.empty();
        }
        final var valor = data.path(locationMap.get(chaveLocationMap).asText()).asText("").trim();
        return valor.isEmpty() ? Optional.empty() : Optional.of(valor);
    }

    static String normalizar(String valor) {
        final var semAcento = Normalizer.normalize(Optional.ofNullable(valor).orElse(""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }

    private static boolean isBlank(String valor) {
        return isNull(valor) || valor.isBlank();
    }

    private static boolean isEmail(String valor) {
        return !isBlank(valor) && valor.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }
}
