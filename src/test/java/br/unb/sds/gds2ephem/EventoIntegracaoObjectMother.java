package br.unb.sds.gds2ephem;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;

public class EventoIntegracaoObjectMother {
    public static EventoIntegracao createIntegrationEvent() {
        final var eventoIntegracao = new EventoIntegracao();
        eventoIntegracao.setId(1);
        eventoIntegracao.setSignalId(1L);
        eventoIntegracao.setEventSourceId("1");
        eventoIntegracao.setEventSourceLocation("1");
        eventoIntegracao.setEventSourceLocationId(1L);
        eventoIntegracao.setUserId(1L);
        eventoIntegracao.setUserEmail("example@test.com");
        eventoIntegracao.setUserName("example");
        eventoIntegracao.setUserPhone("99999999999999");
        return eventoIntegracao;
    }
}
