package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static br.unb.sds.gds2ephem.ephem.EphemAdapter.EQUALS_IGNORECASE_COMPARATOR;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@Slf4j
@Component
@RequiredArgsConstructor
public class EphemMapper {
    private final EphemPort ephemPort;
    private final Clock clock;
    private static final String DEFAULT_ZONE_ID = "America/Sao_Paulo";
    private static final
    Map<String, String> COUNTRY_ZONE_ID_MAP = Map.ofEntries(
            Map.entry("brazil", DEFAULT_ZONE_ID),
            Map.entry("brasil", DEFAULT_ZONE_ID),
            Map.entry("cabo verde", "Atlantic/Cape_Verde"),
            Map.entry("cape verde", "Atlantic/Cape_Verde"),
            Map.entry("portugal", "Europe/Lisbon"),
            Map.entry("angola", "Africa/Luanda"),
            Map.entry("mozambique", "Africa/Maputo"),
            Map.entry("timor leste", "Asia/Dili"),
            Map.entry("east timor", "Asia/Dili"),
            Map.entry("guinea-bissau", "Africa/Bissau"),
            Map.entry("sao tome and principe", "Africa/Sao_Tome"),
            Map.entry("guinea equatorial", "Africa/Malabo")
    );

    public JsonNode mapearData(EventoIntegracao eventoIntegracao) {
        final var input = eventoIntegracao.getData();
        final var mapaVariaveis = eventoIntegracao.getEventoIntegracaoTemplate().getInputEphemMap();

        final var objectMapper = new ObjectMapper();

        final var inputAlterado = objectMapper.createObjectNode();

        mapaVariaveis.fields().forEachRemaining(entry -> {
            final var variavel = entry.getKey();
            final var variavelInfo = entry.getValue();
            final var from = variavelInfo.get("from");
            final var type = variavelInfo.get("type").asText();
            JsonNode valorCorrespondente = new TextNode("");
            if (from.isArray() && !from.isEmpty()) {
                ArrayNode valorCorrespondenteArray = JsonNodeFactory.instance.arrayNode();
                for (JsonNode fromValue : from) {
                    if (input.has(fromValue.asText())) {
                        if (from.size() > 1 || type.equals("model_array")) {
                            valorCorrespondenteArray.add(input.get(fromValue.asText()));
                            valorCorrespondente = valorCorrespondenteArray;
                        } else {
                            valorCorrespondente = input.get(fromValue.asText());
                        }
                    } else if (variavelInfo.has("default_value")) {
                        if (from.size() > 1 || type.equals("model_array")) {
                            valorCorrespondenteArray.add(variavelInfo.get("default_value"));
                            valorCorrespondente = valorCorrespondenteArray;
                        } else {
                            valorCorrespondente = variavelInfo.get("default_value");
                        }
                    }
                }
            } else if (variavelInfo.has("default_value")) {
                valorCorrespondente = variavelInfo.get("default_value");
            }

            if ("date".equals(type)) {
                if (valorCorrespondente.asText().contains("@today")) {
                    valorCorrespondente = new TextNode(LocalDate.now(clock).toString());
                } else {
                    final var fromFormat = variavelInfo.get("from_format").asText();
                    final var inputFormatter = DateTimeFormatter.ofPattern(fromFormat);
                    final var outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    final var localDate = LocalDate.parse(valorCorrespondente.asText(), inputFormatter);
                    final var outputDateString = localDate.format(outputFormatter);
                    valorCorrespondente = new TextNode(outputDateString);
                }
            } else if ("datetime".equals(type)) {
                final var fromFormat = variavelInfo.get("from_format");
                final var userCountry = eventoIntegracao.getUserCountry();
                var zoneId = DEFAULT_ZONE_ID;
                if (COUNTRY_ZONE_ID_MAP.containsKey(Optional.ofNullable(userCountry).orElse("").toLowerCase())) {
                    assert userCountry != null;
                    zoneId = COUNTRY_ZONE_ID_MAP.get(userCountry.toLowerCase());
                }
                if (fromFormat.isArray()) {
                    for (JsonNode format : fromFormat) {
                        try {
                            valorCorrespondente = extractDateTime(valorCorrespondente, format.asText(), zoneId);
                            break;
                        } catch (DateTimeParseException e) {
                            log.warn("Was not possible to parse date {} with format {}", valorCorrespondente, format.asText());
                        }
                    }
                } else {
                    valorCorrespondente = extractDateTime(valorCorrespondente, fromFormat.asText(), zoneId);
                }
            } else if ("string".equals(type)) {
                valorCorrespondente = new TextNode(valorCorrespondente.asText());
            } else if ("integer".equals(type)) {
                try {
                    int intValue = Integer.parseInt(valorCorrespondente.asText());
                    valorCorrespondente = JsonNodeFactory.instance.numberNode(intValue);
                } catch (NumberFormatException e) {
                    valorCorrespondente = variavelInfo.get("default_value");
                }
            } else if ("model".equals(type) || "model_array".equals(type)) {
                final var modelId = variavelInfo.get("model_name");
                final var filterName = variavelInfo.get("model_property_filter");
                if (valorCorrespondente.getNodeType() == JsonNodeType.ARRAY) {
                    ArrayNode valorCorrespondenteArray = JsonNodeFactory.instance.arrayNode();
                    for (JsonNode element : valorCorrespondente) {
                        final var idRetornado = buscarModels(variavelInfo, modelId, filterName, element);
                        valorCorrespondente = valorCorrespondenteArray.add(new IntNode(idRetornado));
                    }
                } else {
                    final var idRetornado = buscarModels(variavelInfo, modelId, filterName, valorCorrespondente);
                    valorCorrespondente = new IntNode(idRetornado);
                }
            }
            // vamos checar se valorCorresponde é um ArrayNode. Se For um ArrayNode e seu conteudo for vazio, nulo ou só contem IntNode com valor 0, então não vamos adicionar ao inputAlterado
            if (valorCorrespondente.getNodeType() == JsonNodeType.ARRAY) {
                boolean isEmpty = true;
                for (JsonNode node : valorCorrespondente) {
                    if (node.getNodeType() == JsonNodeType.OBJECT || (node.getNodeType() == JsonNodeType.NUMBER && node.asInt() != 0)) {
                        isEmpty = false;
                        break;
                    }
                }
                if (!isEmpty) {
                    inputAlterado.set(variavel, valorCorrespondente);
                }
            } else {
                inputAlterado.set(variavel, valorCorrespondente);
            }
        });
        log.info("input alterado {}", inputAlterado);
        return inputAlterado;
    }

    private JsonNode extractDateTime(JsonNode valorCorrespondente, String fromFormat, String zoneId) {
        if (valorCorrespondente.asText().contains("@now")) {
            final var outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            valorCorrespondente = new TextNode(LocalDateTime.now(clock).format(outputFormatter));
        } else {
            final var outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                final var inputFormatter = DateTimeFormatter.ofPattern(fromFormat);
                final var localDateTime = LocalDateTime.parse(valorCorrespondente.asText(), inputFormatter);
                final var localDateTimeUTC = localDateTime.atZone(ZoneId.of(zoneId)).withZoneSameInstant(ZoneId.of("UTC"));
                final var outputDateTimeString = localDateTimeUTC.format(outputFormatter);
                valorCorrespondente = new TextNode(outputDateTimeString);
            } catch (DateTimeParseException e) {
                final var inputFormatter = DateTimeFormatter.ofPattern(fromFormat);
                final var localDate = LocalDate.parse(valorCorrespondente.asText(), inputFormatter);
                final var startOfDay = localDate.atStartOfDay().plusHours(4);
                final var startOfDayUTC = startOfDay.plusSeconds(clock.getZone().getRules().getOffset(startOfDay).getTotalSeconds());
                final var outputDateTimeString = startOfDayUTC.format(outputFormatter);
                valorCorrespondente = new TextNode(outputDateTimeString);
            }
        }
        return valorCorrespondente;
    }

    private Integer buscarModels(JsonNode variavelInfo, JsonNode modelId, JsonNode filterName, JsonNode element) {
        var idRetornado = buscarRegistroModelo(modelId.asText(), filterName.asText(), element.asText());
        if (idRetornado == 0 && variavelInfo.get("default_value").asText().contains("@search")) {
            final var pattern = Pattern.compile("'(.*?)'");
            final var matcher = pattern.matcher(variavelInfo.get("default_value").asText());

            if (matcher.find()) {
                String extractedText = matcher.group(1);
                return buscarRegistroModelo(modelId.asText(), filterName.asText(), extractedText);
            }
        }
        return idRetornado;
    }

    public Integer buscarRegistroModelo(final String modelId, final String modelPropertyFilter, final String valor) {
        final var paramters = EphemParameters.builder()
                .nomeModelo(modelId)
                .fields(emptyList())
                .build();
        final var filtros = List.of(List.of(
                asList(modelPropertyFilter, EQUALS_IGNORECASE_COMPARATOR, valor)));
        paramters.setFiltros(filtros);
        paramters.setContextLang("pt_BR");
        try {
            final var modelos = ephemPort.listarRegistros(paramters);
            if (!modelos.isEmpty()) {
                return Integer.parseInt(((HashMap<String, Object>) modelos.get(0)).get("id").toString());
            }
            return 0;
        } catch (Exception e) {
            log.error("Não foi possível encontrar o registro para o modelo {} com filtro em {} com valor {}", modelId, modelPropertyFilter, valor, e);
            return 0;
        }
    }

    public Integer findCountryByName(final String valor) {
        final var countryModelId = "res.country";
        final var modelPropertyFilter = "name";
        return buscarRegistroModelo(countryModelId, modelPropertyFilter, valor);
    }

    public Integer findStateByNameAndCountryId(final String valor, final Integer countryId) {
        final var stateModelId = "res.country.state";
        final var modelPropertyFilter = "name";
        final var countryIdFilter = "country_id";
        final var filtros = List.of(List.of(
                asList(modelPropertyFilter, EQUALS_IGNORECASE_COMPARATOR, valor),
                asList(countryIdFilter, EQUALS_IGNORECASE_COMPARATOR, countryId.toString())));
        final var paramters = EphemParameters.builder()
                .nomeModelo(stateModelId)
                .fields(emptyList())
                .filtros(filtros)
                .contextLang("pt_BR")
                .build();
        try {
            final var modelos = ephemPort.listarRegistros(paramters);
            if (!modelos.isEmpty()) {
                return Integer.parseInt(((HashMap<String, Object>) modelos.get(0)).get("id").toString());
            }
            return 0;
        } catch (Exception e) {
            log.error("Não foi possível encontrar o registro para o modelo {} com filtro em {} com valor {}", stateModelId, modelPropertyFilter, valor, e);
            return 0;
        }
    }

    public Integer findDistrictByNameAndStateId(final String valor, final Integer stateId) {
        final var districtModelId = "res.country.state.district";
        final var modelPropertyFilter = "name";
        final var stateIdFilter = "state_id";
        final var filtros = List.of(List.of(
                asList(modelPropertyFilter, EQUALS_IGNORECASE_COMPARATOR, valor),
                asList(stateIdFilter, EQUALS_IGNORECASE_COMPARATOR, stateId.toString())));
        final var paramters = EphemParameters.builder()
                .nomeModelo(districtModelId)
                .fields(emptyList())
                .filtros(filtros)
                .contextLang("pt_BR")
                .build();
        try {
            final var modelos = ephemPort.listarRegistros(paramters);
            if (!modelos.isEmpty()) {
                return Integer.parseInt(((HashMap<String, Object>) modelos.get(0)).get("id").toString());
            }
            return 0;
        } catch (Exception e) {
            log.error("Não foi possível encontrar o registro para o modelo {} com filtro em {} com valor {}", districtModelId, modelPropertyFilter, valor, e);
            return 0;
        }
    }
}
