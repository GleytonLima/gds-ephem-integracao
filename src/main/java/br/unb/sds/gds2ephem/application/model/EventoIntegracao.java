package br.unb.sds.gds2ephem.application.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
}
