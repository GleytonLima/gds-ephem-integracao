package br.unb.sds.gds2ephem.application.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "mensagem", collectionRelation = "mensagens")
public class SinalMensagem {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    private Object subject;

    @JsonProperty("signal_id")
    private Integer signalId;

    @JsonProperty("message_type")
    private String messageType;

    private Integer id;

    private String body;

    @JsonProperty("partner_ids")
    private List<Integer> partnerIds;

    @JsonProperty("author_id")
    private Author author;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Author {
        private Integer id;
        private String name;
    }

    public static List<SinalMensagem> fromListOfMaps(List<HashMap<String, Object>> maps) {
        return maps.stream().map(SinalMensagem::mapToSinalMensagem).collect(Collectors.toList());
    }

    private static SinalMensagem mapToSinalMensagem(HashMap<String, Object> map) {
        SinalMensagem message = new SinalMensagem();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final var policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        message.setDate(parseLocalDateTime(map, "date", formatter));
        message.setSubject(sanitizeSubject(map.get("subject"), policy));
        message.setSignalId(getInteger(map, "res_id"));
        message.setMessageType(getString(map, "message_type"));
        message.setId(getInteger(map, "id"));
        message.setBody(policy.sanitize(getString(map, "body")));
        message.setPartnerIds(getIntegerList(map, "partner_ids"));
        message.setAuthor(createAuthor(map.get("author_id")));

        return message;
    }

    private static LocalDateTime parseLocalDateTime(HashMap<String, Object> map, String key, DateTimeFormatter formatter) {
        return Optional.ofNullable(getString(map, key))
                .map(dateStr -> LocalDateTime.parse(dateStr, formatter))
                .orElse(null);
    }

    private static String sanitizeSubject(Object subject, PolicyFactory policy) {
        return Optional.ofNullable(subject)
                .map(obj -> obj instanceof Boolean ? "" : policy.sanitize((String) obj))
                .orElse(null);
    }

    private static Integer getInteger(HashMap<String, Object> map, String key) {
        return (Integer) map.get(key);
    }

    private static String getString(HashMap<String, Object> map, String key) {
        return (String) map.get(key);
    }

    private static List<Integer> getIntegerList(HashMap<String, Object> map, String key) {
        return Optional.ofNullable((Object[]) map.get(key))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(Integer.class::cast)
                .collect(Collectors.toList());
    }

    private static Author createAuthor(Object authorData) {
        if (authorData instanceof Object[] && ((Object[]) authorData).length >= 2) {
            Object[] data = (Object[]) authorData;
            return new Author(getIntegerFromArray(data, 0), getStringFromArray(data, 1));
        }
        return null;
    }

    private static Integer getIntegerFromArray(Object[] array, int index) {
        return (Integer) array[index];
    }

    private static String getStringFromArray(Object[] array, int index) {
        return (String) array[index];
    }
}