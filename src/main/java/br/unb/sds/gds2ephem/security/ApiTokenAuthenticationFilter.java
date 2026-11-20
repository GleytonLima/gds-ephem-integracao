package br.unb.sds.gds2ephem.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Autentica a requisicao pelo header Authorization: "Bearer &lt;token&gt;" ou so "&lt;token&gt;"
 * (o backend do GDS envia o valor configurado na tela de integracao como esta).
 * Token desconhecido ou ausente: segue sem autenticacao e o Spring Security responde 401.
 */
@Slf4j
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {
    static final String ROLE_API_CLIENT = "ROLE_API_CLIENT";
    private static final String BEARER = "bearer ";

    private final Map<String, String> tokensPorValor;

    public ApiTokenAuthenticationFilter(Map<String, String> tokensPorValor) {
        this.tokensPorValor = Map.copyOf(tokensPorValor);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final var token = extrairToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (token != null) {
            final var consumidor = consumidorDoToken(token);
            if (consumidor != null) {
                final var auth = new UsernamePasswordAuthenticationToken(consumidor, null,
                        List.of(new SimpleGrantedAuthority(ROLE_API_CLIENT)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                log.warn("token invalido em {} {} (origem {})", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            }
        }
        chain.doFilter(request, response);
    }

    static String extrairToken(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        final var valor = header.trim();
        return valor.toLowerCase(Locale.ROOT).startsWith(BEARER) ? valor.substring(BEARER.length()).trim() : valor;
    }

    /**
     * Compara todos os tokens em tempo constante, para nao revelar por tempo de resposta qual prefixo acertou.
     */
    private String consumidorDoToken(String token) {
        final var recebido = token.getBytes(StandardCharsets.UTF_8);
        String encontrado = null;
        for (Map.Entry<String, String> e : tokensPorValor.entrySet()) {
            if (MessageDigest.isEqual(recebido, e.getKey().getBytes(StandardCharsets.UTF_8))) {
                encontrado = e.getValue();
            }
        }
        return encontrado;
    }
}
