package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.Objects.isNull;

@Service
public class NarrativeSignalService {

    public static final String EMPTY_STRING = "";

    public String buildNarrativeDescription(final EventoIntegracao eventoIntegracao) {
        final var mapaTabela = eventoIntegracao.extrairTableData();
        return buildHeaderMessage(eventoIntegracao) + buildHtmlTable(mapaTabela);
    }

    public String buildHeaderMessage(final EventoIntegracao eventoIntegracao) {
        final var zoneId = ZoneId.of("America/Sao_Paulo");
        final var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(zoneId);
        final var formattedInstant = formatter.format(eventoIntegracao.getCreatedAt());

        final var stringBuilder = new StringBuilder();

        final var userName = eventoIntegracao.getUserName() != null ? eventoIntegracao.getUserName() : "";
        final var userEmail = eventoIntegracao.getUserEmail() != null ? eventoIntegracao.getUserEmail() : "não informado";
        final var userPhone = eventoIntegracao.getUserPhone() != null ? eventoIntegracao.getUserPhone() : "não informado";

        stringBuilder.append("<br>")
                .append("<p>")
                .append("Reporte pelo App GDS em ")
                .append(HtmlUtils.htmlEscape(formattedInstant))
                .append(", horário de Brasília");

        if (!userName.isEmpty()) {
            stringBuilder.append(", de ")
                    .append(userName);
        }

        stringBuilder.append(" (email: ")
                .append("<a href=\"")
                .append(HtmlUtils.htmlEscape(userEmail))
                .append("\">")
                .append(HtmlUtils.htmlEscape(userEmail))
                .append("</a>")
                .append("; telefone: ")
                .append(HtmlUtils.htmlEscape(userPhone))
                .append(")")
                .append("</p>");

        return stringBuilder.toString();
    }

    public String buildHtmlTable(Map<String, String> dataMap) {
        if (isNull(dataMap) || dataMap.isEmpty()) {
            return EMPTY_STRING;
        }
        final var tableHtml = new StringBuilder();

        tableHtml.append("<table class=\"table table-bordered o_table\">");

        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            final var attribute = entry.getKey();
            final var value = entry.getValue();

            tableHtml.append("<tr>");
            tableHtml.append("<td><p>").append(HtmlUtils.htmlEscape(attribute)).append("</p></td>");
            tableHtml.append("<td><p>").append(HtmlUtils.htmlEscape(value)).append("</p></td>");
            tableHtml.append("</tr>");
        }

        tableHtml.append("</table>");

        return tableHtml.toString();
    }
}

