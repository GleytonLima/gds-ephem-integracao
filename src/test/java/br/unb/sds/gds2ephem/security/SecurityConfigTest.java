package br.unb.sds.gds2ephem.security;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.httpcontroller.EphemSignalsController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Seguranca da API: aberta por padrao; com integracao.auth.enabled=true exige um token de consumidor.
 */
class SecurityConfigTest {
    static final String TOKEN_GDS = "token-do-gds-com-mais-de-24-caracteres";
    static final String TOKEN_ETL = "token-do-etl-com-mais-de-24-caracteres";

    static MockHttpServletRequestBuilder signals() {
        return get("/signals").param("size", "10").param("page", "0");
    }

    @Nested
    @WebMvcTest(EphemSignalsController.class)
    @Import(SecurityConfig.class)
    @DisplayName("integracao.auth.enabled=false (padrao)")
    class Desligada {
        @Autowired
        MockMvc mockMvc;
        @MockBean
        EphemPort ephemPort;
        @MockBean
        EventoIntegracaoRepository eventoIntegracaoRepository;

        @BeforeEach
        void setUp() {
            when(eventoIntegracaoRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(Page.empty());
            when(ephemPort.listarRegistros(any())).thenReturn(Collections.emptyList());
        }

        @Test
        @DisplayName("API aberta: requisicao sem token passa, como antes")
        void semToken() throws Exception {
            mockMvc.perform(signals()).andExpect(status().isOk());
        }
    }

    @Nested
    @WebMvcTest(EphemSignalsController.class)
    @Import(SecurityConfig.class)
    @TestPropertySource(properties = {
            "integracao.auth.enabled=true",
            "integracao.auth.tokens=gds:" + TOKEN_GDS + ", etl:" + TOKEN_ETL
    })
    @DisplayName("integracao.auth.enabled=true")
    class Ligada {
        @Autowired
        MockMvc mockMvc;
        @MockBean
        EphemPort ephemPort;
        @MockBean
        EventoIntegracaoRepository eventoIntegracaoRepository;

        @BeforeEach
        void setUp() {
            when(eventoIntegracaoRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(Page.empty());
            when(ephemPort.listarRegistros(any())).thenReturn(Collections.emptyList());
        }

        @Test
        @DisplayName("sem token: 401")
        void semToken() throws Exception {
            mockMvc.perform(signals()).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("token desconhecido: 401")
        void tokenInvalido() throws Exception {
            mockMvc.perform(signals().header(HttpHeaders.AUTHORIZATION, "Bearer token-que-nao-existe-aaaaaaaaaa"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Bearer com token de qualquer consumidor: 200")
        void bearer() throws Exception {
            mockMvc.perform(signals().header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN_GDS)).andExpect(status().isOk());
            mockMvc.perform(signals().header(HttpHeaders.AUTHORIZATION, "bearer " + TOKEN_ETL)).andExpect(status().isOk());
        }

        @Test
        @DisplayName("token sem o prefixo Bearer (como o backend do GDS envia): 200")
        void tokenCru() throws Exception {
            mockMvc.perform(signals().header(HttpHeaders.AUTHORIZATION, TOKEN_GDS)).andExpect(status().isOk());
        }

        @Test
        @DisplayName("documentacao continua publica")
        void swaggerPublico() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }
}
