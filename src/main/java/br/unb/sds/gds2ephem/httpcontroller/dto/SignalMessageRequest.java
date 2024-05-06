package br.unb.sds.gds2ephem.httpcontroller.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import org.owasp.html.Sanitizers;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SignalMessageRequest {
    private String message;
    public String getSanitizedMessage() {
        final var policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        return policy.sanitize(message);
    }
}
