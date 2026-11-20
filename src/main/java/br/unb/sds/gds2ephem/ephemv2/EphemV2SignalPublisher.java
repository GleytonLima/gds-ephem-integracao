package br.unb.sds.gds2ephem.ephemv2;

import br.unb.sds.gds2ephem.application.SignalPublisher;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.ephem.NarrativeSignalService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Publica o evento via POST /api/v2/signals.
 * <p>
 * Antes do envio, o {@link EphemV2PlaceResolver} troca os nomes de estado e municipio pelos ids do ePHEM.
 * Se mesmo assim o ePHEM recusar um nivel do lugar (422 em geo.district, geo.state ou geo.country, por nome
 * desconhecido ou ambiguo), reenviamos sem aquele nivel e sem os mais precisos que ele. Assim o
 * signal nao se perde por causa da grafia de um municipio; a equipe do ePHEM completa o lugar depois
 * (o nome original continua na descricao narrativa).
 */
@Slf4j
public class EphemV2SignalPublisher implements SignalPublisher {
    static final List<String> GEO_LEVELS = List.of("district", "state", "country");

    private final EphemV2Client client;
    private final EphemV2SignalMapper mapper;
    private final EphemV2PlaceResolver placeResolver;
    private final NarrativeSignalService narrativeSignalService;

    public EphemV2SignalPublisher(EphemV2Client client, EphemV2SignalMapper mapper, EphemV2PlaceResolver placeResolver,
                                  NarrativeSignalService narrativeSignalService) {
        this.client = client;
        this.mapper = mapper;
        this.placeResolver = placeResolver;
        this.narrativeSignalService = narrativeSignalService;
    }

    @Override
    public Long publicar(EventoIntegracao eventoIntegracao) {
        final var payload = mapper.mapear(eventoIntegracao, narrativeSignalService.buildNarrativeDescription(eventoIntegracao));
        if (payload.has("geo")) {
            placeResolver.resolver((ObjectNode) payload.get("geo"), eventoIntegracao.getId());
        }

        for (int tentativa = 0; tentativa <= GEO_LEVELS.size(); tentativa++) {
            try {
                final var signal = client.criarSignal(payload);
                log.info("evento {} publicado na ePHEM API v2: external_id {}, signal_seq {}, id {}",
                        eventoIntegracao.getId(), signal.path("external_id").asText(),
                        signal.path("signal_seq").asText(), signal.path("id").asLong());
                return signal.path("id").asLong();
            } catch (EphemV2ValidationException e) {
                final var campo = e.firstFieldWithIssue().orElse("");
                if (!campo.startsWith("geo.") || !removerNivelGeo(payload, campo.substring("geo.".length()))) {
                    throw e;
                }
                // um id recusado indica lista em cache desatualizada
                placeResolver.invalidar(payload.path("geo").path("country").asText(""));
                log.warn("evento {}: ePHEM recusou {} ({}); reenviando sem esse nivel", eventoIntegracao.getId(), campo, e.getMessage());
            }
        }
        throw new IllegalStateException("evento " + eventoIntegracao.getId() + ": nao foi possivel resolver o lugar no ePHEM");
    }

    /**
     * Remove o nivel recusado e todos os mais precisos (um distrito nao faz sentido sem o estado dele).
     *
     * @return false se nao havia nada para remover (evita laco)
     */
    static boolean removerNivelGeo(ObjectNode payload, String nivel) {
        final var indice = GEO_LEVELS.indexOf(nivel);
        if (indice < 0 || !payload.has("geo")) {
            return false;
        }
        final var geo = (ObjectNode) payload.get("geo");
        var removeu = false;
        for (int i = 0; i <= indice; i++) {
            removeu |= geo.remove(GEO_LEVELS.get(i)) != null;
        }
        if (geo.size() == 0) {
            payload.remove("geo");
        }
        return removeu;
    }
}
