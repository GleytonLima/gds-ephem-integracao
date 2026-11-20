package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.ConfiguracaoSistemaRepository;
import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.model.ConfiguracaoSistema;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static br.unb.sds.gds2ephem.application.model.ConfiguracaoSistema.DEFAULT_SYSTEM_CONFIG_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotaInicialServiceTest {
    private static final long SIGNAL_ID = 191L;
    private static final long GDS_PARTNER_ID = 2097L;

    @Mock
    private EphemPort ephemPort;
    @Mock
    private ConfiguracaoSistemaRepository configuracaoSistemaRepository;
    @InjectMocks
    private NotaInicialService service;

    private EventoIntegracao evento;

    @BeforeEach
    void setUp() {
        evento = new EventoIntegracao();
        evento.setId(10L);
    }

    private void comConfiguracao() {
        final var config = new ConfiguracaoSistema();
        config.setEphemGdsUserId(GDS_PARTNER_ID);
        when(configuracaoSistemaRepository.findById(DEFAULT_SYSTEM_CONFIG_ID)).thenReturn(Optional.of(config));
    }

    @Test
    @DisplayName("registra a nota de historico como usuario GDS, com nome e email de quem relatou")
    void registraNota() {
        comConfiguracao();
        evento.setUserName("João Ramos");
        evento.setUserEmail("joao.ramos@email.com");

        service.registrar(evento, SIGNAL_ID);

        verify(ephemPort).criarNotaHistorico(SIGNAL_ID, GDS_PARTNER_ID,
                "<p>Relato recebido do Guardiões da Saúde (GDS). Reportado por <b>João Ramos</b> (joao.ramos@email.com).</p>");
    }

    @Test
    @DisplayName("sem usuario GDS configurado, nao registra nada")
    void semConfiguracao() {
        when(configuracaoSistemaRepository.findById(DEFAULT_SYSTEM_CONFIG_ID)).thenReturn(Optional.empty());

        service.registrar(evento, SIGNAL_ID);

        verifyNoInteractions(ephemPort);
    }

    @Test
    @DisplayName("falha no ePHEM nao propaga: o signal ja foi criado")
    void falhaNaoPropaga() {
        comConfiguracao();
        when(ephemPort.criarNotaHistorico(anyLong(), anyLong(), anyString())).thenThrow(new RuntimeException("timeout"));

        assertThatCode(() -> service.registrar(evento, SIGNAL_ID)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("monta a nota escapando HTML e tratando nome ou email ausentes")
    void montarNota() {
        evento.setUserName("<script>x</script>");
        assertThat(NotaInicialService.montarNota(evento))
                .isEqualTo("<p>Relato recebido do Guardiões da Saúde (GDS). Reportado por <b>&lt;script&gt;x&lt;/script&gt;</b>.</p>");

        evento.setUserName(" ");
        evento.setUserEmail("a@b.com");
        assertThat(NotaInicialService.montarNota(evento)).endsWith("Reportado por <b>a@b.com</b>.</p>");

        evento.setUserEmail(null);
        assertThat(NotaInicialService.montarNota(evento)).endsWith("Reportado por usuário não identificado.</p>");
    }
}
