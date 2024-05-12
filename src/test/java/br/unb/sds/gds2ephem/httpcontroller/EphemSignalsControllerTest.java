package br.unb.sds.gds2ephem.httpcontroller;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.configs.WebSecurityConfiguration;
import br.unb.sds.gds2ephem.ephem.EphemParameters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EphemSignalsController.class)
@Import(WebSecurityConfiguration.class)
class EphemSignalsControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EphemPort ephemPort;

    @MockBean
    private EventoIntegracaoRepository eventoIntegracaoRepository;

    @Test
    @WithMockUser
    void testGetSignals_EmptyData() throws Exception {
        when(ephemPort.listarRegistros(any(EphemParameters.class))).thenReturn(Collections.emptyList());
        when(eventoIntegracaoRepository.findByUserId(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/signals")
                        .param("size", "10")
                        .param("page", "0")
                        .param("user_id", "1"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$._embedded.signals").doesNotExist())
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    @WithMockUser
    void testGetSignals_NonEmptyData() throws Exception {
        List<Object> sampleData = new ArrayList<>();
        Map<String, Object> signal1 = new HashMap<>();
        signal1.put("id", 1);
        // Add other fields as required for Signal class
        sampleData.add(signal1);

        when(ephemPort.listarRegistros(any(EphemParameters.class))).thenReturn(sampleData);
        when(eventoIntegracaoRepository.findByUserId(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/signals")
                        .param("size", "10")
                        .param("page", "0")
                        .param("user_id", "1"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$._embedded.signals").isArray())
                .andExpect(jsonPath("$._embedded.signals.length()").value(1))
                .andExpect(jsonPath("$._embedded.signals[0].signalId").value(1));
    }
}
