package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import br.unb.sds.gds2ephem.application.model.exceptions.EphemAuthException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Component
@Profile("!local")
public class EphemAdapter implements EphemPort {
    public static final String EXECUTE_KW = "execute_kw";
    public static final List<Object> TODOS_OS_CAMPOS = emptyList();
    public static final List<Object> SEM_FILTRO = emptyList();
    public static final String MODEL_PARAMETER_NAME = "model";
    public static final String MODULES_PARAMETER_NAME = "modules";
    public static final String ILIKE_COMPARATOR = "ilike";
    public static final String EQUALS_IGNORECASE_COMPARATOR = "=ilike";
    public static final String FIELDS_PARAMETER_NAME = "fields";
    public static final String LIMIT_PARAMETER_NAME = "limit";

    public static final String OFFSET_PARAMETER_NAME = "offset";
    public static final String SEARCH_READ_REMOTE_PROCEDURE_NAME = "search_read";
    public static final String ORDER_BY_PARAMETER_NAME = "order";
    public static final String EOC_SIGNAL = "eoc.signal";
    public static final String CONTEXT_PARAMETER_NAME = "context";
    public static final String CONTEXT_LANG_KEY = "lang";
    public static final String PT_BR = "pt_BR";

    private final XmlRpcClient xmlRpcClientObject;
    private final XmlRpcClient xmlRpcClientCommon;

    public EphemAdapter(@Qualifier("xmlRpcClientObject") XmlRpcClient xmlRpcClientObject, @Qualifier("xmlRpcClientCommon") XmlRpcClient xmlRpcClientCommon) {
        this.xmlRpcClientObject = xmlRpcClientObject;
        this.xmlRpcClientCommon = xmlRpcClientCommon;
    }

    @Value("${odoo.db}")
    private String db;

    @Value("${odoo.user}")
    private String odooUser;

    @Value("${odoo.apikey}")
    private String odooApiKey;
    private Integer uid;

    @Override
    public void autenticar() {
        if (isNull(this.uid)) {
            try {
                Object[] params = new Object[]{db, odooUser, odooApiKey, new Object[]{}};
                this.uid = (int) this.xmlRpcClientCommon.execute("authenticate", params);
                log.info("Autenticado com sucesso: {}", this.uid);
            } catch (Exception e) {
                log.error("Falha na autenticacao no Ephem (Odoo)", e);
                throw new EphemAuthException(e.getMessage());
            }
        }
    }

    @Override
    @SneakyThrows
    public Object listarAtributosPorNomeModelo(final String modelName) {
        autenticar();
        final var dadosPesquisa = Map.ofEntries(
                entry("attributes", asList("string", "help", "type"))
        );
        final var parametrosChamadaRemota = asList(
                db,
                uid,
                odooApiKey,
                modelName,
                "fields_get",
                SEM_FILTRO,
                dadosPesquisa
        );
        final var atributos = this.xmlRpcClientObject.execute(EXECUTE_KW, parametrosChamadaRemota);
        log.info("Atributos recuperados do modelo {}", atributos);
        return atributos;
    }

    @Override
    @SneakyThrows
    public List<Object> listarRegistros(EphemParameters ephemParameters) {
        autenticar();
        final var dadosPesquisa = Map.ofEntries(
                entry(FIELDS_PARAMETER_NAME, Optional.ofNullable(ephemParameters.getFields()).orElse(emptyList())),
                entry(LIMIT_PARAMETER_NAME, Optional.ofNullable(ephemParameters.getSize()).orElse(10)),
                entry(OFFSET_PARAMETER_NAME, Optional.ofNullable(ephemParameters.getOffset()).orElse(0)),
                entry(ORDER_BY_PARAMETER_NAME, Optional.ofNullable(ephemParameters.getSort()).orElse("")),
                entry(CONTEXT_PARAMETER_NAME, Map.ofEntries(
                        entry(CONTEXT_LANG_KEY, Optional.ofNullable(ephemParameters.getContextLang()).orElse(PT_BR))
                ))
        );
        var filtros = Optional.ofNullable(ephemParameters.getFiltros()).orElse(SEM_FILTRO);
        if (nonNull(ephemParameters.getId())) {
            filtros = List.of(List.of(
                    List.of("id", "=", ephemParameters.getId())));
        }
        final var parametrosExecucaoRemota = asList(
                db, uid, odooApiKey,
                ephemParameters.getNomeModelo(), SEARCH_READ_REMOTE_PROCEDURE_NAME,
                filtros,
                dadosPesquisa
        );
        return asList((Object[]) this.xmlRpcClientObject.execute(EXECUTE_KW, parametrosExecucaoRemota));
    }

    @Override
    @SneakyThrows
    public Object recuperarVersaoInstanciaOdoo() {
        return this.xmlRpcClientCommon.execute("version", emptyList());
    }

    @Override
    @SneakyThrows
    public List<Object> listarModelos(final String modulesFiltro, final String modelFiltro) {
        this.autenticar();
        final var filtros = List.of(asList(
                asList(MODULES_PARAMETER_NAME, ILIKE_COMPARATOR, modulesFiltro),
                asList(MODEL_PARAMETER_NAME, ILIKE_COMPARATOR, modelFiltro)));
        final var dadosPesquisa = Map.ofEntries(
                entry(FIELDS_PARAMETER_NAME, asList("id", MODEL_PARAMETER_NAME, "name", MODULES_PARAMETER_NAME)),
                entry(LIMIT_PARAMETER_NAME, 1000)
        );
        return asList((Object[]) this.xmlRpcClientObject.execute(EXECUTE_KW, asList(
                db,
                uid,
                odooApiKey,
                "ir.model",
                SEARCH_READ_REMOTE_PROCEDURE_NAME,
                filtros,
                dadosPesquisa
        )));
    }

    @Override
    @SneakyThrows
    public List<Object> listarCamposModelo(final String modeloNome, final String atributoNome) {
        this.autenticar();
        final var dadosPesquisa = Map.ofEntries(
                entry(FIELDS_PARAMETER_NAME, TODOS_OS_CAMPOS),
                entry(LIMIT_PARAMETER_NAME, 1000)
        );
        final var filtros = List.of(asList(
                asList(MODEL_PARAMETER_NAME, "=", modeloNome), asList("name", "=", atributoNome)));
        return asList((Object[]) this.xmlRpcClientObject.execute(EXECUTE_KW, asList(
                db, uid, odooApiKey,
                "ir.model.fields",
                SEARCH_READ_REMOTE_PROCEDURE_NAME,
                filtros,
                dadosPesquisa
        )));
    }

    @Override
    @SneakyThrows
    public Long criarSignal(final Map<String, Object> dados) {
        this.autenticar();
        final var parametros = asList(db, uid, odooApiKey, EOC_SIGNAL, "create", List.of(dados));
        final var signalId = ((Integer) this.xmlRpcClientObject.execute(EXECUTE_KW, parametros)).longValue();
        log.info("signal criado com suceso com id: {}", signalId);
        try {
            final var mensagem = "Este registro foi gerado a partir do aplicativo Guardiões da Saúde";
            final var mensagemCriadaId = criarMensagem(signalId, mensagem);
            log.info("mensagem padrao gerada com sucesso com o id {}", mensagemCriadaId);
        } catch (Exception e) {
            log.error("erro ao criar mensagem padrao. excecao nao sera relancada para que o signal nao seja criado novamente", e);
        }
        return signalId;
    }

    @Override
    @SneakyThrows
    public Long criarMensagem(final Long signalId, final String mensagem) {
        this.autenticar();
        final var mensagemParametros = Map.ofEntries(
                entry("body", mensagem),
                entry("author_id", uid),
                entry("message_type", "comment"),
                entry(MODEL_PARAMETER_NAME, EOC_SIGNAL),
                entry("res_id", signalId)
        );
        final var executeParametros = asList(db, uid, odooApiKey, "mail.message", "create", List.of(mensagemParametros));

        return ((Integer) this.xmlRpcClientObject.execute(EXECUTE_KW, executeParametros)).longValue();
    }

    @Override
    public HashMap<String, Object> consultarSignalPorId(Long id) {
        final var emphemParameters = EphemParameters.builder()
                .nomeModelo(EOC_SIGNAL)
                .fields(asList("id", "signal_stage_state_id"))
                .id(id)
                .build();
        final var list = this.listarRegistros(emphemParameters);
        if (list.isEmpty()) {
            return new HashMap<>();
        }
        return (HashMap<String, Object>) list.get(0);
    }

    @Override
    @SneakyThrows
    public void deletarSignalPorId(Long signalId) {
        this.autenticar();
        final var executeParametros = asList(db, uid, odooApiKey, EOC_SIGNAL, "unlink", List.of(List.of(signalId)));

        this.xmlRpcClientObject.execute(EXECUTE_KW, executeParametros);
    }


    public Integer getUid() {
        return uid;
    }
}
