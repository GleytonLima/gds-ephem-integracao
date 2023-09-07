package br.unb.sds.gds2ephem.httpcontroller;


import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.ephem.EphemParameters;
import br.unb.sds.gds2ephem.httpcontroller.dto.Signal;
import lombok.RequiredArgsConstructor;
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
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/signals", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
public class EphemSignalsController {
    private final EphemPort ephemPort;

    @GetMapping
    public CollectionModel<?> getSignals(@RequestParam("size") Integer size, @RequestParam("page") Integer page) {
        final var offset = page * size;
        final var campos = SIGNAL_DEFAULT_PARAMETERS;
        final var parameters = EphemParameters.builder()
                .nomeModelo(SIGNAL_MODEL_NAME)
                .fields(campos)
                .sort(MODEL_DEFAULT_SORT)
                .offset(offset)
                .size(size)
                .build();

        final var dados = ephemPort.listarRegistros(parameters);

        if (dados.isEmpty()) {
            final var link = linkTo(Signal.class).withSelfRel();
            return CollectionModel.of(emptyList(), link);
        }
        final var signals = dados.stream()
                .map(s -> (HashMap<String, Object>) s)
                .map(s -> new Signal(null, Long.parseLong(s.get("id").toString()), s))
                .collect(Collectors.toList());
        final var link = linkTo(Signal.class).withSelfRel();
        return CollectionModel.of(List.of(signals), link);
    }
}

