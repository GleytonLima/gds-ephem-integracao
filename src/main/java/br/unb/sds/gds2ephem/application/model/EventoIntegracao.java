package br.unb.sds.gds2ephem.application.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Data
@TypeDef(name = "json", typeClass = JsonType.class)
@EntityListeners(AuditingEntityListener.class)
public class EventoIntegracao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "template_id")
    private EventoIntegracaoTemplate eventoIntegracaoTemplate;
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private JsonNode data;
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private JsonNode aditionalData;
    private String status = "CRIADO";
    private String statusMessage = "";
    private Long signalId;
    private String eventSourceId;
    private String eventSourceLocation;
    private Long userId;
    private String userEmail;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public Map<String, String> extrairTableData() {
        final var mapper = new ObjectMapper();
        return mapper.convertValue(aditionalData, new TypeReference<>() {
        });
    }

    public void mapearData() {
        JsonNode input = data;
        JsonNode mapaVariaveis = eventoIntegracaoTemplate.getInputEphemMap();

        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode inputAlterado = objectMapper.createObjectNode();

        mapaVariaveis.fields().forEachRemaining(entry -> {
            String variavel = entry.getKey();
            JsonNode variavelInfo = entry.getValue();

            if (variavelInfo.has("from") && variavelInfo.get("from").isArray()) {
                JsonNode fromValues = variavelInfo.get("from");
                for (JsonNode fromValue : fromValues) {
                    if (input.has(fromValue.asText())) {
                        JsonNode valorCorrespondente = input.get(fromValue.asText());
                        //TODO: Verificar questao de formatacao e tipos
                        inputAlterado.set(variavel, valorCorrespondente);
                        break;
                    }
                }
            } else if (variavelInfo.has("default_value")) {
                JsonNode valorPadrao = variavelInfo.get("default_value");
                inputAlterado.set(variavel, valorPadrao);
            }
        });

        System.out.println(inputAlterado.toString());

        this.data = inputAlterado;
    }
}
