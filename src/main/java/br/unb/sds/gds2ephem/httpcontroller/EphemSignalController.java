package br.unb.sds.gds2ephem.httpcontroller;


import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.httpcontroller.dto.Signal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/eventos", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
public class EphemSignalController {
    private final EphemPort ephemPort;
    private final EventoIntegracaoRepository eventoIntegracaoRepository;

    private final PagedResourcesAssembler<Signal> pagedAssembler;

    @GetMapping("/{id}/signals")
    public CollectionModel<?> getSignals(@PathVariable("id") Long id) {
        final var evento = eventoIntegracaoRepository.findById(id);
        if (evento.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "evento nao encontrado");
        }
        final var dados = ephemPort.consultarSignalPorId(evento.get().getSignalId());
        if (dados.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "signal nao encontrado");
        }
        final var signals = new Signal(id, dados);
        final var link = linkTo(Signal.class).withSelfRel();
        return CollectionModel.of(List.of(signals), link);
    }
}

