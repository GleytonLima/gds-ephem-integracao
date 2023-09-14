package br.unb.sds.gds2ephem.httpcontroller;


import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.ephem.EphemParameters;
import br.unb.sds.gds2ephem.ephem.EphemUtils;
import br.unb.sds.gds2ephem.httpcontroller.dto.Model;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static br.unb.sds.gds2ephem.ephem.EphemAdapter.EQUALS_IGNORECASE_COMPARATOR;
import static br.unb.sds.gds2ephem.ephem.EphemAdapter.ILIKE_COMPARATOR;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/models", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
@Slf4j
public class EphemModelsController {
    private final EphemPort ephemPort;

    @GetMapping("/{modelId}")
    public CollectionModel<?> getModels(@PathVariable("modelId") String modelId,
                                        @RequestParam(value = "filter_name", required = false) String filterName,
                                        @RequestParam(value = "filter_value", required = false) String filterValue,
                                        @RequestParam(value = "filter_comparator", required = false) String filterComparator,
                                        @RequestParam(value = "size", required = false) Integer size,
                                        @RequestParam(value = "offset", required = false) Integer offset) {
        Locale locale = LocaleContextHolder.getLocale();
        log.info("Locale request {}", locale);
        final var paramters = EphemParameters.builder()
                .nomeModelo(modelId)
                .contextLang(locale.toString())
                .size(size)
                .offset(offset)
                .fields(emptyList())
                .build();
        if (nonNull(filterName) && nonNull(filterValue) && nonNull(filterComparator)) {
            final var filtros = asList(asList(
                    asList(filterName, filterComparator, filterValue)));
            paramters.setFiltros(filtros);
        }
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

