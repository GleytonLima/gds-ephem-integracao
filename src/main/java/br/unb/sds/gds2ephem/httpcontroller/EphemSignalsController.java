package br.unb.sds.gds2ephem.httpcontroller;


import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.ephem.EphemParameters;
import br.unb.sds.gds2ephem.httpcontroller.dto.Signal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static br.unb.sds.gds2ephem.ephem.EphemParameters.*;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/signals", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
public class EphemSignalsController {
    private final EphemPort ephemPort;
    private final EventoIntegracaoRepository eventoIntegracaoRepository;

    @GetMapping
    public CollectionModel<?> getSignals(@RequestParam("size") Integer size, @RequestParam("page") Integer page, @RequestParam(value = "user_id") Long userId) {
        final var offset = page * size;
        final var parametersBuilder = EphemParameters.builder()
                .nomeModelo(SIGNAL_MODEL_NAME)
                .fields(SIGNAL_DEFAULT_PARAMETERS)
                .sort(MODEL_DEFAULT_SORT)
                .offset(offset)
                .size(size);

        final var eventos = eventoIntegracaoRepository.findByUserId(PageRequest.of(page, size), userId);
        final var signalIdsArray = eventos.stream()
                .map(EventoIntegracao::getSignalId)
                .toArray(Object[]::new);
        parametersBuilder.idList(signalIdsArray);

        final var parameters = parametersBuilder.build();

        final var dados = ephemPort.listarRegistros(parameters);

        if (dados.isEmpty()) {
            final var link = linkTo(Signal.class).withSelfRel();
            return CollectionModel.of(emptyList(), link);
        }
        final var signals = dados.stream()
                .map(s -> (HashMap<String, Object>) s)
                .map(s -> new Signal(null, Long.parseLong(s.get("id").toString()), s))
                .collect(Collectors.toList());

        eventos.forEach(evento -> {
            final var signal = signals.stream()
                    .filter(s -> s.getSignalId().equals(evento.getSignalId()))
                    .findFirst();
            signal.ifPresent(value -> value.setEventId(evento.getId()));
        });

        final var link = linkTo(Signal.class).withSelfRel();
        return CollectionModel.of(List.of(signals), link);
    }
}

