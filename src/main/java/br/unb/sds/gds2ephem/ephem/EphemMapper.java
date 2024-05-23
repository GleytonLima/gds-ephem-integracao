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
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(fromFormat);
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate localDate = LocalDate.parse(valorCorrespondente.asText(), inputFormatter);
                    String outputDateString = localDate.format(outputFormatter);
                    valorCorrespondente = new TextNode(outputDateString);
                }
            } else if ("datetime".equals(type)) {
                final var fromFormat = variavelInfo.get("from_format");
                if (fromFormat.isArray()) {
                    for (JsonNode format : fromFormat) {
                        try {
                            valorCorrespondente = extractDateTime(valorCorrespondente, format.asText());
                            break;
                        } catch (DateTimeParseException e) {
                            log.warn("Was not possible to parse date {} with format {}", valorCorrespondente, format.asText());
                        }
                    }
                } else {
                    valorCorrespondente = extractDateTime(valorCorrespondente, fromFormat.asText());
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

    private JsonNode extractDateTime(JsonNode valorCorrespondente, String fromFormat) {
        if (valorCorrespondente.asText().contains("@now")) {
            final var outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            valorCorrespondente = new TextNode(LocalDateTime.now(clock).format(outputFormatter));
        } else {
            final var outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                final var inputFormatter = DateTimeFormatter.ofPattern(fromFormat);
                final var localDateTime = LocalDateTime.parse(valorCorrespondente.asText(), inputFormatter);
                final var localDateTimeUTC = localDateTime.atZone(ZoneId.of("America/Manaus")).withZoneSameInstant(ZoneId.of("UTC"));
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
}
