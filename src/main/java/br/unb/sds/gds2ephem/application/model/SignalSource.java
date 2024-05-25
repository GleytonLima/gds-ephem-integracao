package br.unb.sds.gds2ephem.application.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "source", collectionRelation = "sources")
public class SignalSource {
    @JsonProperty("signal_id")
    private Long signalId;

    @JsonProperty("source_type")
    private Long sourceType;

    @JsonProperty("source_name")
    private String sourceName;

    @JsonProperty("sourceAddress")
    private String sourceAddress;
}