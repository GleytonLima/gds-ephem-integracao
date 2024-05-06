package br.unb.sds.gds2ephem.httpcontroller;


import br.unb.sds.gds2ephem.application.ConfiguracaoSistemaRepository;
import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.EventoIntegracaoRepository;
import br.unb.sds.gds2ephem.application.model.EventoIntegracaoStatus;
import br.unb.sds.gds2ephem.application.model.SinalMensagem;
import br.unb.sds.gds2ephem.ephem.EphemMessageParameters;
import br.unb.sds.gds2ephem.httpcontroller.dto.Signal;
import br.unb.sds.gds2ephem.httpcontroller.dto.SignalMessageRequest;
import br.unb.sds.gds2ephem.httpcontroller.dto.SignalMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;

import static br.unb.sds.gds2ephem.application.model.ConfiguracaoSistema.DEFAULT_SYSTEM_CONFIG_ID;
import static java.util.Objects.isNull;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/eventos", produces = MediaType.APPLICATION_JSON_VALUE)
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
public class EphemEventosSignalsController {
    private final EphemPort ephemPort;
    private final EventoIntegracaoRepository eventoIntegracaoRepository;
    private final ConfiguracaoSistemaRepository configuracaoSistemaRepository;
    private final PagedResourcesAssembler<SinalMensagem> assembler;

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

    @GetMapping("/{eventId}/mensagens")
    public EntityModel<PagedModel<EntityModel<SinalMensagem>>> getMessages(@PathVariable("eventId") Long eventId, @RequestParam("size") Integer size, @RequestParam("page") Integer page) {
        final var config = configuracaoSistemaRepository.findById(DEFAULT_SYSTEM_CONFIG_ID);
        if (config.isEmpty()) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "configuracao nao efetuada. contate o administrador do sistema");
        }
        final var idUsuarioGdsApp = config.get().getEphemGdsUserId();
        final var adminUserId = config.get().getEphemAdminUserId();
        final var evento = eventoIntegracaoRepository.findById(eventId);
        if (evento.isEmpty() || evento.get().getStatus().equals(EventoIntegracaoStatus.DELETADO.name())) {
            throw new ResponseStatusException(NOT_FOUND, "evento nao encontrado");
        }
        final var filters = EphemMessageParameters.builder()
                .fromIds(new Object[]{idUsuarioGdsApp, adminUserId})
                .signalIds(new Object[]{evento.get().getSignalId()})
                .partnerIds(new Object[]{idUsuarioGdsApp, adminUserId})
                .limit(Long.valueOf(size))
                .offset((long) page * (long) size)
                .build();
        final var mensagens = ephemPort.listarMensagens(filters);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<SinalMensagem> messagePage = new PageImpl<>(mensagens, pageRequest, mensagens.size()); // Simula a paginação

        PagedModel<EntityModel<SinalMensagem>> pagedModel = assembler.toModel(messagePage, EntityModel::of);

        return EntityModel.of(pagedModel);
    }


    @PostMapping("{eventId}/mensagens")
    public ResponseEntity<SignalMessageResponse> criarMensagem(@PathVariable("eventId") Long eventId, @RequestBody SignalMessageRequest mensagem) {
        final var config = configuracaoSistemaRepository.findById(DEFAULT_SYSTEM_CONFIG_ID);
        if (config.isEmpty()) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "configuracao nao efetuada. contate o administrador do sistema");
        }
        final var idUsuarioGdsApp = config.get().getEphemGdsUserId();
        final var adminUserId = config.get().getEphemAdminUserId();
        final var evento = eventoIntegracaoRepository.findById(eventId);
        if (evento.isEmpty() || evento.get().getStatus().equals(EventoIntegracaoStatus.DELETADO.name())) {
            throw new ResponseStatusException(NOT_FOUND, "evento nao encontrado");
        }
        ephemPort.criarMensagem(evento.get().getSignalId(), idUsuarioGdsApp, mensagem.getSanitizedMessage(), adminUserId);
        return ResponseEntity.ok(new SignalMessageResponse(mensagem.getMessage()));
    }
}

