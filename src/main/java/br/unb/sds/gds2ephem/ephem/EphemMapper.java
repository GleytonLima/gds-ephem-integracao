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

import static br.unb.sds.gds2ephem.ephem.EphemAdapter.EQUALS_COMPARATOR;
import static br.unb.sds.gds2ephem.ephem.EphemAdapter.EQUALS_IGNORECASE_COMPARATOR;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class EphemMapper {
    public static final String PT_BR = "pt_BR";
    private final EphemPort ephemPort;
    private final Clock clock;
    private static final String DEFAULT_ZONE_ID = "America/Sao_Paulo";
    public static final String SOURCE_COUNTRY_ID = "source_country_id";
    public static final String COUNTRY_ID_KEY = "country_ids";
    public static final String STATE_ID_KEY = "state_ids";
    public static final String DISTRICT_IDS = "district_ids";
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
            Map.entry("guiné-bissau", "Africa/Bissau"),
            Map.entry("sao tome and principe", "Africa/Sao_Tome"),
            Map.entry("guiné-equatorial", "Africa/Malabo")
    );

    Map<String, String> COUNTRY_TRANSLATION_MAP = Map.ofEntries(
            Map.entry("brazil", "brasil"),
            Map.entry("cape verde", "cabo verde"),
            Map.entry("east timor", "timor leste"),
            Map.entry("guinea-bissau", "guiné-bissau"),
            Map.entry("equatorial guinea", "guiné-equatorial"),
            Map.entry("sao tome and principe", "sao tome e principe")
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
        mapLocationFields(inputAlterado, eventoIntegracao);
        log.info("input to send to ephem {}", inputAlterado);
        return inputAlterado;
    }

    /**
     * (0, 0, {values}): Adds a new record by providing a dictionary of field values.
     * (1, id, {values}): Updates an existing record with the given ID by providing a dictionary of field values.
     * (2, id): Removes the record with the given ID from the set but does not delete it from the database.
     * (3, id): Removes the record with the given ID from the set without deleting it from the database.
     * (4, id): Adds an existing record with the given ID to the set.
     * (5): Removes all records from the set.
     * (6, 0, [ids])
     * @param dadosRequest
     * @param eventoIntegracao
     */
    public void mapLocationFields(ObjectNode dadosRequest, EventoIntegracao eventoIntegracao) {
        final var countryFromGds = eventoIntegracao.getUserCountry();
        if (isNull(countryFromGds)) {
            return;
        }
        final var countryIdFromEphem = findCountryByName(countryFromGds);
        if (isNull(countryIdFromEphem) || countryIdFromEphem == 0) {
            return;
        }
        dadosRequest.set(SOURCE_COUNTRY_ID, new IntNode(countryIdFromEphem));
        ArrayNode countryArray = JsonNodeFactory.instance.arrayNode();

        final var countriesIdsArray = new ArrayNode(JsonNodeFactory.instance);
        countriesIdsArray.add(new IntNode(countryIdFromEphem));

        countryArray.add(new IntNode(6));
        countryArray.add(new IntNode(0));
        countryArray.add(countriesIdsArray);

        dadosRequest.set(COUNTRY_ID_KEY, new ArrayNode(JsonNodeFactory.instance).add(countryArray));
        final var fieldStateSourceName = eventoIntegracao.getEventoIntegracaoTemplate().getLocationMap().get("state_source_field").asText();
        if (!eventoIntegracao.getData().has(fieldStateSourceName)) {
            return;
        }
        final var stateFromGds = eventoIntegracao.getData().get(fieldStateSourceName).asText();
        final var stateIdFromEphem = this.findStateByNameAndCountryId(stateFromGds, countryIdFromEphem);
        if (nonNull(stateIdFromEphem) && stateIdFromEphem > 0) {
            final var stateArray = JsonNodeFactory.instance.arrayNode();
            final var statesIdsArray = new ArrayNode(JsonNodeFactory.instance);
            statesIdsArray.add(new IntNode(stateIdFromEphem));

            stateArray.add(new IntNode(6));
            stateArray.add(new IntNode(0));
            stateArray.add(statesIdsArray);
            dadosRequest.set(STATE_ID_KEY, new ArrayNode(JsonNodeFactory.instance).add(stateArray));
        }
        final var fieldDistrictSourceName = eventoIntegracao.getEventoIntegracaoTemplate().getLocationMap().get("district_source_field").asText();
        if (!eventoIntegracao.getData().has(fieldDistrictSourceName)) {
            return;
        }
        final var cityFromGds = eventoIntegracao.getData().get(fieldDistrictSourceName).asText();
        final var cityIdFromEphem = this.findDistrictByNameAndStateId(cityFromGds, stateIdFromEphem);
        if (nonNull(cityIdFromEphem) && cityIdFromEphem > 0) {
            ArrayNode cityArray = JsonNodeFactory.instance.arrayNode();
            cityArray.add(new IntNode(6));
            cityArray.add(new IntNode(0));
            cityArray.add(new ArrayNode(JsonNodeFactory.instance).add(cityIdFromEphem));
            dadosRequest.set(DISTRICT_IDS, new ArrayNode(JsonNodeFactory.instance).add(cityArray));
        }
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
        paramters.setContextLang(PT_BR);
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

    public Integer findCountryByName(String valor) {
        final var countryModelId = "res.country";
        final var modelPropertyFilter = "name";
        if (COUNTRY_TRANSLATION_MAP.containsKey(valor.toLowerCase())) {
            valor = COUNTRY_TRANSLATION_MAP.get(valor.toLowerCase());
        }
        return buscarRegistroModelo(countryModelId, modelPropertyFilter, valor);
    }

    public Integer findStateByNameAndCountryId(final String valor, final Integer countryId) {
        final var stateModelId = "res.country.state";
        final var modelPropertyFilter = "name";
        final var countryIdFilter = "country_id";
        final var filtros = List.of(List.of(
                asList(modelPropertyFilter, EQUALS_IGNORECASE_COMPARATOR, valor),
                asList(countryIdFilter, EQUALS_COMPARATOR, countryId)));
        final var paramters = EphemParameters.builder()
                .nomeModelo(stateModelId)
                .fields(emptyList())
                .filtros(filtros)
                .contextLang(PT_BR)
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
                asList(stateIdFilter, EQUALS_COMPARATOR, stateId)));
        final var paramters = EphemParameters.builder()
                .nomeModelo(districtModelId)
                .fields(emptyList())
                .filtros(filtros)
                .contextLang(PT_BR)
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
