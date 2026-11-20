package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.ConfiguracaoSistemaRepository;
import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static br.unb.sds.gds2ephem.application.model.ConfiguracaoSistema.DEFAULT_SYSTEM_CONFIG_ID;
import static org.springframework.util.StringUtils.hasText;

/**
 * Logo apos criar o signal, registra no historico do ePHEM quem fez o relato no GDS. Sem isso o historico so mostra
 * "GDS App" (dono da chave da API); o nome de quem relatou ficava apenas na descricao.
 * <p>
 * Uma falha aqui nao desfaz nada: o signal ja existe, entao so registramos em log.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotaInicialService {
    private final EphemPort ephemPort;
    private final ConfiguracaoSistemaRepository configuracaoSistemaRepository;

    public void registrar(EventoIntegracao eventoIntegracao, Long signalId) {
        try {
            final var config = configuracaoSistemaRepository.findById(DEFAULT_SYSTEM_CONFIG_ID);
            if (config.isEmpty() || config.get().getEphemGdsUserId() == null) {
                log.warn("evento {}: sem usuario GDS configurado; nota inicial nao registrada", eventoIntegracao.getId());
                return;
            }
            ephemPort.criarNotaHistorico(signalId, config.get().getEphemGdsUserId(), montarNota(eventoIntegracao));
        } catch (Exception e) {
            log.warn("evento {}: falha ao registrar a nota inicial no signal {}", eventoIntegracao.getId(), signalId, e);
        }
    }

    static String montarNota(EventoIntegracao eventoIntegracao) {
        final var nome = hasText(eventoIntegracao.getUserName()) ? eventoIntegracao.getUserName().trim() : null;
        final var email = hasText(eventoIntegracao.getUserEmail()) ? eventoIntegracao.getUserEmail().trim() : null;

        final var sb = new StringBuilder("<p>Relato recebido do Guardiões da Saúde (GDS). Reportado por ");
        if (nome == null && email == null) {
            sb.append("usuário não identificado");
        } else {
            sb.append("<b>").append(HtmlUtils.htmlEscape(nome != null ? nome : email, UTF_8.name())).append("</b>");
            if (nome != null && email != null) {
                sb.append(" (").append(HtmlUtils.htmlEscape(email, UTF_8.name())).append(")");
            }
        }
        return sb.append(".</p>").toString();
    }
}
