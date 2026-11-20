package br.unb.sds.gds2ephem.application.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SinalMensagemTest {

    @Test
    @DisplayName("Converte o retorno do search_read de mail.message, sanitizando o HTML")
    void converteMensagemCompleta() {
        final var map = new HashMap<String, Object>();
        map.put("id", 99);
        map.put("res_id", 555);
        map.put("date", "2026-11-18 10:30:00");
        map.put("subject", "<i>Assunto</i><script>x()</script>");
        map.put("message_type", "comment");
        map.put("body", "<p>Texto <a href=\"https://exemplo.org\">link</a></p><img src=x onerror=alert(1)>");
        map.put("partner_ids", new Object[]{3, 7});
        map.put("author_id", new Object[]{7, "GDS App"});

        final var mensagem = SinalMensagem.fromListOfMaps(List.of(map)).get(0);

        assertThat(mensagem.getId()).isEqualTo(99);
        assertThat(mensagem.getSignalId()).isEqualTo(555);
        assertThat(mensagem.getDate()).isEqualTo(LocalDateTime.of(2026, 11, 18, 10, 30));
        assertThat(mensagem.getSubject()).isEqualTo("<i>Assunto</i>");
        assertThat(mensagem.getMessageType()).isEqualTo("comment");
        assertThat(mensagem.getBody()).contains("Texto").contains("href=\"https://exemplo.org\"")
                .doesNotContain("<img").doesNotContain("onerror");
        assertThat(mensagem.getPartnerIds()).containsExactly(3, 7);
        assertThat(mensagem.getAuthor()).isEqualTo(new SinalMensagem.Author(7, "GDS App"));
    }

    @Test
    @DisplayName("Odoo devolve false para campos vazios: subject false vira string vazia; ausentes viram null ou lista vazia")
    void converteMensagemMinima() {
        final var map = new HashMap<String, Object>();
        map.put("id", 1);
        map.put("subject", false);
        map.put("author_id", false);

        final var mensagem = SinalMensagem.fromListOfMaps(List.of(map)).get(0);

        assertThat(mensagem.getSubject()).isEqualTo("");
        assertThat(mensagem.getDate()).isNull();
        assertThat(mensagem.getPartnerIds()).isEmpty();
        assertThat(mensagem.getAuthor()).isNull();
    }
}
