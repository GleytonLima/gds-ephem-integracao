package br.unb.sds.gds2ephem.httpcontroller.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import org.owasp.html.Sanitizers;
import org.springframework.web.util.HtmlUtils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.StringUtils.hasText;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SignalMessageRequest {
    private String message;
    /**
     * Opcionais: quem escreveu a mensagem no GDS. No ePHEM a nota sai como "GDS App"; com estes campos ela
     * ganha um cabecalho dizendo quem escreveu.
     */
    private String authorName;
    private String authorEmail;

    public String getSanitizedMessage() {
        final var policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
        return policy.sanitize(message);
    }

    /**
     * Texto sanitizado, precedido de "<b>Nome</b> (email) via GDS:" quando o autor foi informado.
     */
    public String getMensagemComAutor() {
        return cabecalhoAutor() + getSanitizedMessage();
    }

    String cabecalhoAutor() {
        final var nome = hasText(authorName) ? authorName.trim() : null;
        final var email = hasText(authorEmail) ? authorEmail.trim() : null;
        if (nome == null && email == null) {
            return "";
        }
        final var sb = new StringBuilder("<p><b>").append(HtmlUtils.htmlEscape(nome != null ? nome : email, UTF_8.name())).append("</b>");
        if (nome != null && email != null) {
            sb.append(" (").append(HtmlUtils.htmlEscape(email, UTF_8.name())).append(")");
        }
        return sb.append(" via GDS:</p>").toString();
    }
}
