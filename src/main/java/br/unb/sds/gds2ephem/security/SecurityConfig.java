package br.unb.sds.gds2ephem.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguranca da API do integrador.
 * <ul>
 *     <li>integracao.auth.enabled=false (padrao): API aberta, como sempre foi. Um aviso e registrado no log.</li>
 *     <li>integracao.auth.enabled=true: toda requisicao precisa de um dos tokens de integracao.auth.tokens,
 *     exceto a documentacao (swagger). Sem token valido: 401.</li>
 * </ul>
 * Substitui WebSecurityConfiguration (JWT do Firebase, nunca ativada) e WebSecurityDisabledConfiguration.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ApiAuthProperties.class)
public class SecurityConfig {
    static final String[] ROTAS_PUBLICAS = {
            "/v2/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/webjars/**", "/error"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiAuthProperties properties) throws Exception {
        http.csrf().disable()
                .httpBasic().disable()
                .formLogin().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        if (!properties.isEnabled()) {
            log.warn("integracao.auth.enabled=false: a API do integrador esta ABERTA (sem autenticacao)");
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
            return http.build();
        }

        final var tokens = properties.tokensPorValor();
        if (tokens.isEmpty()) {
            throw new IllegalStateException("integracao.auth.enabled=true exige integracao.auth.tokens (INTEGRACAO_AUTH_TOKENS)");
        }
        log.info("autenticacao da API ligada para os consumidores {}", tokens.values());
        http.addFilterBefore(new ApiTokenAuthenticationFilter(tokens), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(authz -> authz
                        .antMatchers(ROTAS_PUBLICAS).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
