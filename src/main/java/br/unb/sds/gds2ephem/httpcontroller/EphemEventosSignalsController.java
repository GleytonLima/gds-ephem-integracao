package br.unb.sds.gds2ephem.httpcontroller;


import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoStatus;
import br.unb.sds.gds2ephem.httpcontroller.dto.Signal;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/eventos", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
public class EphemEventosSignalsController {
    private final EphemPort ephemPort;
    private final EventoIntegracaoRepository eventoIntegracaoRepository;

    @GetMapping("/{eventId}/signals")
    public CollectionModel<?> getSignalsByEventId(@PathVariable("eventId") Long eventId) {
        final var evento = eventoIntegracaoRepository.findById(eventId);
        if (evento.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "evento nao encontrado");
        }
        if (evento.get().getStatus().equals(EventoIntegracaoStatus.CRIADO.name())) {
            final var dadosEmProcessamento = new HashMap<String, Object>();
            dadosEmProcessamento.put("signal_stage_state_id", List.of(0, "Informado"));
            final var signals = new Signal(eventId, 0L, dadosEmProcessamento);
            final var link = linkTo(Signal.class).withSelfRel();
            return CollectionModel.of(List.of(signals), link);
        }
        if (isNull(evento.get().getSignalId())) {
            throw new ResponseStatusException(NOT_FOUND, "signal nao encontrado");
        }
        final var dados = ephemPort.consultarSignalPorId(evento.get().getSignalId());
        if (dados.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "signal nao encontrado");
        }
        final var signals = new Signal(eventId, Long.parseLong(dados.get("id").toString()), dados);
        final var link = linkTo(Signal.class).withSelfRel();
        return CollectionModel.of(List.of(signals), link);
    }

    @DeleteMapping("/{eventId}/signals/{signalId}")
    public ResponseEntity<String> deleteSignalsByEventId(@PathVariable("eventId") Long eventId, @PathVariable("signalId") Long signalId) {
        final var evento = eventoIntegracaoRepository.findById(eventId);
        if (evento.isEmpty() || evento.get().getStatus().equals(EventoIntegracaoStatus.DELETADO.name())) {
            throw new ResponseStatusException(NOT_FOUND, "evento nao encontrado");
        }
        ephemPort.deletarSignalPorId(evento.get().getSignalId());
        evento.get().setStatus(EventoIntegracaoStatus.DELETADO.name());
        eventoIntegracaoRepository.save(evento.get());
        return ResponseEntity.ok("signal deletado com sucesso");
    }
}

