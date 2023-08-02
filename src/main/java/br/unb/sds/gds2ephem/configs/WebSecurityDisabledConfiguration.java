package br.unb.sds.gds2ephem.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//@Profile("!localhomol & !local")
public class WebSecurityDisabledConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz ->
                authz.antMatchers("/**").permitAll()
                        .anyRequest().authenticated());
        http.oauth2ResourceServer().jwt();
        return http.build();
    }
}
