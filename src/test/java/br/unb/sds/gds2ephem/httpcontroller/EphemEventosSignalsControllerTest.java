package br.unb.sds.gds2ephem.httpcontroller;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoStatus;
import br.unb.sds.gds2ephem.configs.WebSecurityConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(EphemEventosSignalsController.class)
@Import(WebSecurityConfiguration.class)
class EphemEventosSignalsControllerTest {
    @MockBean
    private JwtDecoder jwtDecoder;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventoIntegracaoRepository eventoIntegracaoRepository;
    @MockBean
    private EphemPort ephemPort;

    @Test
    @DisplayName("Deve retornar 200")
    @WithMockUser(authorities = {"my.scope"})
    void testGetSignalById_SignalFound() throws Exception {
        final var id = 1L;
        final var dados = new HashMap<String, Object>() {{
            put("id", 1);
            put("general_hazard_id", false);
            put("confidentiality", "pheoc");
            put("specific_hazard_id", 2);
            put("state_id", false);
            put("signal_type", "opening");
            put("report_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            put("incident_date", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        }};
        final var evento = new EventoIntegracao();
        evento.setId(10);
        evento.setSignalId(1L);
        when(eventoIntegracaoRepository.findById(anyLong())).thenReturn(Optional.of(evento));
        when(ephemPort.consultarSignalPorId(id)).thenReturn(dados);

        mockMvc.perform(MockMvcRequestBuilders.get("/eventos/{id}/signals", id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.signals[0].eventId").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.signals[0].dados").exists());

        verify(ephemPort).consultarSignalPorId(id);
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found")
    @WithMockUser(authorities = {"my.scope"})
    void testGetSignalById_SignalNotFound() throws Exception {
        final var eventoId = 2L;
        final var signalId = 1L;
        final var evento = new EventoIntegracao();
        evento.setId(eventoId);
        evento.setSignalId(signalId);
        when(eventoIntegracaoRepository.findById(eventoId)).thenReturn(Optional.of(evento));
        when(ephemPort.consultarSignalPorId(signalId)).thenReturn(new HashMap<>());

        mockMvc.perform(MockMvcRequestBuilders.get("/eventos/{id}/signals", eventoId))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertEquals("404 NOT_FOUND \"signal nao encontrado\"", Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(ephemPort).consultarSignalPorId(signalId);
        verify(eventoIntegracaoRepository).findById(eventoId);
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found para Evento")
    @WithMockUser(authorities = {"my.scope"})
    void testGetSignalByIdEventoNaoEncontrado() throws Exception {
        final var id = 2L;
        final var evento = new EventoIntegracao();
        evento.setId(10);
        evento.setSignalId(1L);
        when(eventoIntegracaoRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/eventos/{id}/signals", id))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
                .andExpect(result -> assertEquals("404 NOT_FOUND \"evento nao encontrado\"", Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(eventoIntegracaoRepository).findById(id);
    }

    @Test
    @DisplayName("Deve deletar com sucesso")
    @WithMockUser(authorities = {"my.scope"})
    void deleteSignalsByEventId() throws Exception {
        final var eventId = 1L;
        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setId(eventId);
        eventoIntegracao.setStatus(EventoIntegracaoStatus.CRIADO.name());

        when(eventoIntegracaoRepository.findById(eventId)).thenReturn(Optional.of(eventoIntegracao));

        mockMvc.perform(MockMvcRequestBuilders.delete("/eventos/{eventId}/signals/{signalId}", eventId, 1)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("signal deletado com sucesso"));

        assertEquals(EventoIntegracaoStatus.DELETADO.name(), eventoIntegracao.getStatus());
        verify(ephemPort).deletarSignalPorId(eventoIntegracao.getSignalId());
        verify(eventoIntegracaoRepository).save(eventoIntegracao);
    }

    @Test
    @DisplayName("Deve deletar com erro nao encontrado dado que o evento não esta na base")
    @WithMockUser(authorities = {"my.scope"})
    void testDeleteSignalsByEventId_SignalNotFound() throws Exception {
        final var eventId = 1L;
        when(eventoIntegracaoRepository.findById(eventId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/eventos/{eventId}/signals/{signalId}", eventId, 1)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(eventoIntegracaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar com erro nao encontrado dado que evento já foi deletado")
    @WithMockUser(authorities = {"my.scope"})
    void testDeleteSignalsByEventId_SignalNotFoundDeletado() throws Exception {
        final var eventId = 1L;
        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setId(eventId);
        eventoIntegracao.setStatus(EventoIntegracaoStatus.DELETADO.name());
        when(eventoIntegracaoRepository.findById(eventId)).thenReturn(Optional.of(eventoIntegracao));

        mockMvc.perform(MockMvcRequestBuilders.delete("/eventos/{eventId}/signals/{signalId}", eventId, 1)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        verify(eventoIntegracaoRepository, never()).save(any());
    }
}

