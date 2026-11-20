package br.unb.sds.gds2ephem.application;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;

/**
 * Publica um evento de integracao como signal no ePHEM.
 * <p>
 * Ha duas implementacoes, escolhidas pela propriedade {@code ephem.v2.enabled}:
 * <ul>
 *     <li>XML-RPC (legado, padrao): mapeia o evento pelo template e chama {@code eoc.signal.create}.</li>
 *     <li>API REST v2 do ePHEM: {@code POST /api/v2/signals}, idempotente por {@code external_id}.</li>
 * </ul>
 */
public interface SignalPublisher {
    /**
     * @return o id interno do signal no ePHEM
     */
    Long publicar(EventoIntegracao eventoIntegracao);
}
