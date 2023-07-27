package br.unb.sds.gds2ephem.httpcontroller;


import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.ephem.EphemParameters;
import br.unb.sds.gds2ephem.ephem.EphemUtils;
import br.unb.sds.gds2ephem.httpcontroller.dto.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/models", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
public class EphemModelsController {
    private final EphemPort ephemPort;

    @GetMapping("/{modelId}")
    public CollectionModel<?> getModels(@PathVariable("modelId") String modelId) {
        final var paramters = EphemParameters.builder()
                .nomeModelo(modelId)
                .fields(emptyList())
                .build();

        final var modelos = ephemPort.listarRegistros(paramters);

        final var link = linkTo(Model.class).withSelfRel();
        return CollectionModel.of(modelos.stream().map(Model::new).collect(Collectors.toList()), link);
    }

    @GetMapping("/{modelId}/selections/{selectionId}")
    public CollectionModel<?> getSelections(@PathVariable("modelId") String modelId, @PathVariable("selectionId") String selectionId) {
        final var modelosCampos = ephemPort.listarCamposModelo(modelId, selectionId);
        final var opcoesSelecaoString = ((HashMap) modelosCampos.get(0)).get("selection");
        final var opcoesSelecao = EphemUtils.converterPythonStringTupleEmList((String) opcoesSelecaoString);
        final var link = linkTo(Map.class).withSelfRel();
        return CollectionModel.of(List.of(opcoesSelecao), link);
    }
}

