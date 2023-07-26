package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class NarrativeSignalService {
    public String buildNarrativeDescription(final EventoIntegracao eventoIntegracao) {
        final var mapaTabela = eventoIntegracao.extrairTableData();
        return buildHeaderMessage(eventoIntegracao) + buildHtmlTable(mapaTabela);
    }

    public String buildHeaderMessage(final EventoIntegracao eventoIntegracao) {
        final var zoneId = ZoneId.of("America/Sao_Paulo");
        final var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(zoneId);
        final var formattedInstant = formatter.format(eventoIntegracao.getCreatedAt());

        final var stringBuilder = new StringBuilder();

        stringBuilder.append("<br>")
                .append("<p>")
                .append("Reporte pelo App GDS em ")
                .append(HtmlUtils.htmlEscape(formattedInstant))
                .append(" de ")
                .append("<a href=\"")
                .append(HtmlUtils.htmlEscape(eventoIntegracao.getUserEmail()))
                .append("\">")
                .append(HtmlUtils.htmlEscape(eventoIntegracao.getUserEmail()))
                .append("</a>")
                .append("</p>");

        return stringBuilder.toString();
    }

    public String buildHtmlTable(Map<String, String> dataMap) {
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

