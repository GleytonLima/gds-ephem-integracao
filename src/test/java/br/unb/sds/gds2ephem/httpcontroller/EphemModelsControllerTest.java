package br.unb.sds.gds2ephem.httpcontroller;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.configs.WebSecurityConfiguration;
import br.unb.sds.gds2ephem.ephem.EphemParameters;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.when;

@WebMvcTest(EphemModelsController.class)
@Import(WebSecurityConfiguration.class)
class EphemModelsControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EphemPort ephemPort;

    @Test
    @WithMockUser
    void testGetModels() throws Exception {
        final var modelId = "modelo1";
        when(ephemPort.listarRegistros(Mockito.any(EphemParameters.class)))
                .thenReturn(Collections.singletonList(new HashMap<>()));

        final var result = mockMvc.perform(MockMvcRequestBuilders.get("/models/{modelId}", modelId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    @WithMockUser
    void testGetSelections() throws Exception {
        final var modelId = "modelo1";
        final var selectionId = "selection1";
        final var hashMap = new HashMap<String, Object>();
        hashMap.put("selection", "[('event', 'Event'), ('monitored', 'Monitored then closed'), ('discarded', 'Discarded')]");
        when(ephemPort.listarCamposModelo(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Collections.singletonList(hashMap));

        mockMvc.perform(MockMvcRequestBuilders.get("/models/{modelId}/selections/{selectionId}", modelId, selectionId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }
}