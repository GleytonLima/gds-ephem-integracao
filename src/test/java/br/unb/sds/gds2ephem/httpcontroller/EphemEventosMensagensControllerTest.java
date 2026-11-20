package br.unb.sds.gds2ephem.httpcontroller;

import br.unb.sds.gds2ephem.application.ConfiguracaoSistemaRepository;
import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.application.model.ConfiguracaoSistema;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoStatus;
import br.unb.sds.gds2ephem.application.model.SinalMensagem;
import br.unb.sds.gds2ephem.configs.WebSecurityConfiguration;
import br.unb.sds.gds2ephem.ephem.EphemMessageParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoints de mensagens (chatter do signal). Continuam via XML-RPC mesmo com a API v2 habilitada,
 * porque a v2 nao expoe mail.message.
 */
@WebMvcTest(EphemEventosSignalsController.class)
@Import(WebSecurityConfiguration.class)
class EphemEventosMensagensControllerTest {
    private static final long EVENTO_ID = 10L;
    private static final long SIGNAL_ID = 555L;
    private static final long GDS_USER_ID = 7L;
    private static final long ADMIN_USER_ID = 3L;

    @MockBean
    private JwtDecoder jwtDecoder;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventoIntegracaoRepository eventoIntegracaoRepository;
    @MockBean
    private ConfiguracaoSistemaRepository configuracaoSistemaRepository;
    @MockBean
    private EphemPort ephemPort;

    private EventoIntegracao evento;

    @BeforeEach
    void setUp() {
        final var config = new ConfiguracaoSistema();
        config.setEphemGdsUserId(GDS_USER_ID);
        config.setEphemAdminUserId(ADMIN_USER_ID);
        when(configuracaoSistemaRepository.findById(ConfiguracaoSistema.DEFAULT_SYSTEM_CONFIG_ID)).thenReturn(Optional.of(config));

        evento = new EventoIntegracao();
        evento.setId(EVENTO_ID);
        evento.setSignalId(SIGNAL_ID);
        evento.setStatus(EventoIntegracaoStatus.PROCESSADO.name());
        when(eventoIntegracaoRepository.findById(EVENTO_ID)).thenReturn(Optional.of(evento));
    }

    @Test
    @DisplayName("GET mensagens filtra pelo signal e pelos usuarios GDS/admin, com offset = page * size")
    @WithMockUser(authorities = {"my.scope"})
    void listarMensagens() throws Exception {
        final var mensagem = new SinalMensagem(LocalDateTime.of(2026, 11, 18, 10, 0), "", (int) SIGNAL_ID, "comment", 99,
                "<p>ola</p>", List.of(3), new SinalMensagem.Author(7, "GDS"));
        when(ephemPort.listarMensagens(any())).thenReturn(List.of(mensagem));
        when(ephemPort.contarMensagens(any())).thenReturn(23L);

        mockMvc.perform(get("/eventos/{id}/mensagens", EVENTO_ID).param("size", "5").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(23))
                .andExpect(jsonPath("$.page.totalPages").value(5))
                .andExpect(jsonPath("$.page.number").value(2))
                .andExpect(jsonPath("$._embedded.mensagens[0].id").value(99))
                .andExpect(jsonPath("$._embedded.mensagens[0].body").value("<p>ola</p>"))
                .andExpect(jsonPath("$._embedded.mensagens[0].author_id.name").value("GDS"))
                .andExpect(jsonPath("$._embedded.mensagens[0].date").value("2026-11-18 10:00:00"));

        final var captor = ArgumentCaptor.forClass(EphemMessageParameters.class);
        verify(ephemPort).listarMensagens(captor.capture());
        final var filtros = captor.getValue();
        assertThat(filtros.getSignalIds()).containsExactly(SIGNAL_ID);
        assertThat(filtros.getFromIds()).containsExactly(GDS_USER_ID, ADMIN_USER_ID);
        assertThat(filtros.getPartnerIds()).containsExactly(GDS_USER_ID, ADMIN_USER_ID);
        assertThat(filtros.getLimit()).isEqualTo(5L);
        assertThat(filtros.getOffset()).isEqualTo(10L);
    }

    @Test
    @DisplayName("GET mensagens de evento que ainda nao virou signal: pagina vazia, sem chamar o ePHEM")
    @WithMockUser(authorities = {"my.scope"})
    void listarEventoSemSignal() throws Exception {
        evento.setStatus(EventoIntegracaoStatus.CRIADO.name());
        evento.setSignalId(null);

        mockMvc.perform(get("/eventos/{id}/mensagens", EVENTO_ID).param("size", "5").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$._embedded").doesNotExist());
        verifyNoInteractions(ephemPort);
    }

    @Test
    @DisplayName("POST mensagem para evento que ainda nao virou signal responde 409 sem chamar o ePHEM")
    @WithMockUser(authorities = {"my.scope"})
    void criarMensagemEventoSemSignal() throws Exception {
        evento.setStatus(EventoIntegracaoStatus.CRIADO.name());
        evento.setSignalId(null);

        mockMvc.perform(post("/eventos/{id}/mensagens", EVENTO_ID)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf())
                        .content("{\"message\": \"oi\"}"))
                .andExpect(status().isConflict());
        verifyNoInteractions(ephemPort);
    }

    @Test
    @DisplayName("GET mensagens sem configuracao do sistema responde 422")
    @WithMockUser(authorities = {"my.scope"})
    void listarSemConfiguracao() throws Exception {
        when(configuracaoSistemaRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/eventos/{id}/mensagens", EVENTO_ID).param("size", "5").param("page", "0"))
                .andExpect(status().isUnprocessableEntity());
        verifyNoInteractions(ephemPort);
    }

    @Test
    @DisplayName("GET mensagens de evento deletado responde 404")
    @WithMockUser(authorities = {"my.scope"})
    void listarEventoDeletado() throws Exception {
        evento.setStatus(EventoIntegracaoStatus.DELETADO.name());

        mockMvc.perform(get("/eventos/{id}/mensagens", EVENTO_ID).param("size", "5").param("page", "0"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(ephemPort);
    }

    @Test
    @DisplayName("POST mensagem envia o texto sanitizado como usuario GDS, notificando o admin")
    @WithMockUser(authorities = {"my.scope"})
    void criarMensagem() throws Exception {
        mockMvc.perform(post("/eventos/{id}/mensagens", EVENTO_ID)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf())
                        .content("{\"message\": \"<b>Atualizacao</b><script>alert(1)</script>\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("<b>Atualizacao</b><script>alert(1)</script>"));

        verify(ephemPort).criarMensagem(SIGNAL_ID, GDS_USER_ID, "<b>Atualizacao</b>", ADMIN_USER_ID);
    }

    @Test
    @DisplayName("POST mensagem para evento inexistente responde 404 sem chamar o ePHEM")
    @WithMockUser(authorities = {"my.scope"})
    void criarMensagemEventoInexistente() throws Exception {
        when(eventoIntegracaoRepository.findById(EVENTO_ID)).thenReturn(Optional.empty());

        mockMvc.perform(post("/eventos/{id}/mensagens", EVENTO_ID)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf())
                        .content("{\"message\": \"oi\"}"))
                .andExpect(status().isNotFound());
        verify(ephemPort, never()).criarMensagem(anyLong(), anyLong(), anyString(), anyLong());
    }
}
