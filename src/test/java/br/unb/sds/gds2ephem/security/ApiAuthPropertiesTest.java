package br.unb.sds.gds2ephem.security;

import br.unb.sds.gds2ephem.application.model.ConfiguracaoSistema;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiAuthPropertiesTest {
    private static final String T1 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String T2 = "bbbbbbbbbbbbbbbbbbbbbbbbbbbb";

    private static ApiAuthProperties com(String tokens) {
        final var p = new ApiAuthProperties();
        p.setTokens(tokens);
        return p;
    }

    @Test
    @DisplayName("nome:token separados por virgula, com espacos")
    void lista() {
        assertThat(com(" gds:" + T1 + " , etl:" + T2 + ",").tokensPorValor()).isEqualTo(Map.of(T1, "gds", T2, "etl"));
        assertThat(com("").tokensPorValor()).isEmpty();
        assertThat(com(null).tokensPorValor()).isEmpty();
    }

    @Test
    @DisplayName("formato invalido ou token curto impedem a subida")
    void invalidos() {
        assertThatThrownBy(() -> com("so-um-token-sem-nome-aaaaaaaaaa").tokensPorValor()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> com("gds:").tokensPorValor()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> com("gds:curto").tokensPorValor())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("24");
    }

    @Test
    @DisplayName("Authorization aceita 'Bearer x' e 'x'")
    void extrairToken() {
        assertThat(ApiTokenAuthenticationFilter.extrairToken("Bearer abc")).isEqualTo("abc");
        assertThat(ApiTokenAuthenticationFilter.extrairToken("BEARER  abc ")).isEqualTo("abc");
        assertThat(ApiTokenAuthenticationFilter.extrairToken("abc")).isEqualTo("abc");
        assertThat(ApiTokenAuthenticationFilter.extrairToken("  ")).isNull();
        assertThat(ApiTokenAuthenticationFilter.extrairToken(null)).isNull();
    }

    @Test
    @DisplayName("ephemAdminApiKey pode ser gravada mas nunca sai no JSON")
    void apiKeyNaoSaiNoJson() throws Exception {
        final var mapper = new ObjectMapper();
        final var config = new ConfiguracaoSistema();
        config.setEphemAdminApiKey("segredo");
        config.setEphemGdsUserId(7L);

        final var json = mapper.writeValueAsString(config);
        assertThat(json).doesNotContain("segredo").doesNotContain("ephemAdminApiKey").contains("\"ephemGdsUserId\":7");

        final var lida = mapper.readValue("{\"ephemAdminApiKey\":\"nova\"}", ConfiguracaoSistema.class);
        assertThat(lida.getEphemAdminApiKey()).isEqualTo("nova");
    }
}
