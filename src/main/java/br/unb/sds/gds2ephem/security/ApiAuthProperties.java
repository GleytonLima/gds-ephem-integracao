package br.unb.sds.gds2ephem.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Autenticacao da API do integrador por token estatico, um por consumidor.
 * <p>
 * {@code integracao.auth.tokens} (env INTEGRACAO_AUTH_TOKENS) e uma lista separada por virgula de
 * {@code nome:token}, ex.: {@code gds:abc123,etl:def456}. O nome so aparece em log, para saber quem chamou.
 */
@Data
@ConfigurationProperties(prefix = "integracao.auth")
public class ApiAuthProperties {
    /**
     * false (padrao): API aberta, como antes. Ligar so depois que os consumidores enviarem o token.
     */
    private boolean enabled = false;
    private String tokens = "";

    /**
     * token -> nome do consumidor.
     */
    public Map<String, String> tokensPorValor() {
        final var mapa = new LinkedHashMap<String, String>();
        if (tokens == null || tokens.isBlank()) {
            return mapa;
        }
        for (String entrada : tokens.split(",")) {
            final var item = entrada.trim();
            if (item.isEmpty()) {
                continue;
            }
            final var separador = item.indexOf(':');
            if (separador <= 0 || separador == item.length() - 1) {
                throw new IllegalStateException("integracao.auth.tokens: use nome:token separados por virgula");
            }
            final var nome = item.substring(0, separador).trim();
            final var token = item.substring(separador + 1).trim();
            if (token.length() < 24) {
                throw new IllegalStateException("integracao.auth.tokens: o token de '" + nome + "' precisa ter ao menos 24 caracteres");
            }
            mapa.put(token, nome);
        }
        return mapa;
    }
}
