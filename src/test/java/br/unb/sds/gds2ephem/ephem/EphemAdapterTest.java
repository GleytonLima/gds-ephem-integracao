package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.ConfiguracaoSistemaRepository;
import br.unb.sds.gds2ephem.application.model.exceptions.EphemAuthException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.unb.sds.gds2ephem.EventoIntegracaoObjectMother.createIntegrationEvent;
import static br.unb.sds.gds2ephem.ephem.EphemAdapter.CONTEXT_LANG_KEY;
import static br.unb.sds.gds2ephem.ephem.EphemAdapter.CONTEXT_PARAMETER_NAME;
import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EphemAdapterTest {
    private final String db = "db";
    private final Integer uid = 1;
    private final String odooApiKey = "apikey";
    @Mock
    private XmlRpcClient objectClient;

    @Mock
    private XmlRpcClient commonClient;

    private EphemAdapter adapter;

    @Mock
    private ConfiguracaoSistemaRepository configuracaoSistemaRepository;

    @BeforeEach
    void setUp() {
        adapter = new EphemAdapter(objectClient, commonClient, configuracaoSistemaRepository);
    }

    @Test
    @DisplayName("Deve autenticar com sucesso")
    void autenticar_SetsUidWhenUidIsNull() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", null);
        when(commonClient.execute(anyString(), any(Object[].class))).thenReturn(1);

        adapter.autenticar();

        assertEquals(1, adapter.getUid());
        verify(commonClient, times(1)).execute(eq("authenticate"), any(Object[].class));
    }

    @Test
    @DisplayName("Deve autenticar com erro")
    void autenticar_exception() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", null);
        when(commonClient.execute(anyString(), any(Object[].class))).thenThrow(new RuntimeException());

        assertThrows(EphemAuthException.class, () -> adapter.autenticar());
    }

    @Test
    @DisplayName("Nao deve autenticar se ja estiver autenticado")
    void autenticar_DoesNotSetUidWhenUidIsNotNull() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);

        adapter.autenticar();

        assertEquals(uid, adapter.getUid());
        verify(commonClient, never()).execute(eq("authenticate"), any(Object[].class));
    }

    @Test
    @DisplayName("Deve executar listarAtributosPorNomeModelo com sucesso")
    void listarAtributosPorNomeModelo_CallsModelsClientWithCorrectParameters() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        String modelName = "modelo";

        adapter.listarAtributosPorNomeModelo(modelName);

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        modelName,
                        "fields_get",
                        EphemAdapter.SEM_FILTRO,
                        Map.of("attributes", List.of("string", "help", "type"))
                ))
        );
    }

    @Test
    @DisplayName("Deve executar listarAtributosPorNomeModelo com erro")
    void listarAtributosPorNomeModelo_Erro() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        String modelName = "modelo";
        when(objectClient.execute(anyString(), anyList())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> adapter.listarAtributosPorNomeModelo(modelName));

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        modelName,
                        "fields_get",
                        EphemAdapter.SEM_FILTRO,
                        Map.of("attributes", List.of("string", "help", "type"))
                ))
        );
    }

    @Test
    @DisplayName("Deve executar listarRegistrosPorNomeModelo com sucesso")
    void listarRegistrosPorNomeModelo_CallsModelsClientWithCorrectParameters() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var nomeModelo = "modelo";
        final var fields = List.of("field1", "field2");
        final var parameters = EphemParameters.builder()
                .nomeModelo(nomeModelo)
                .fields(fields)
                .build();
        when(objectClient.execute(anyString(), anyList())).thenReturn(new Object[]{});

        adapter.listarRegistros(parameters);

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        nomeModelo,
                        EphemAdapter.SEARCH_READ_REMOTE_PROCEDURE_NAME,
                        EphemAdapter.SEM_FILTRO,
                        Map.of(
                                EphemAdapter.LIMIT_PARAMETER_NAME, 10,
                                EphemAdapter.OFFSET_PARAMETER_NAME, 0,
                                EphemAdapter.FIELDS_PARAMETER_NAME, fields,
                                CONTEXT_PARAMETER_NAME, Map.ofEntries(
                                        entry(CONTEXT_LANG_KEY, "pt_BR")
                                ),
                                EphemAdapter.ORDER_BY_PARAMETER_NAME, ""
                        )
                ))
        );
    }

    @Test
    @DisplayName("Deve executar listarRegistrosPorNomeModeloComId com id, que resulta em um filtro com id, sucesso")
    void listarRegistrosPorNomeModeloComId() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var nomeModelo = "modelo";
        List<String> fields = List.of("field1", "field2");
        when(objectClient.execute(eq(EphemAdapter.EXECUTE_KW), anyList())).thenReturn(new Object[]{});
        final var parameters = EphemParameters.builder()
                .nomeModelo(nomeModelo)
                .fields(fields)
                .id(1L)
                .build();

        adapter.listarRegistros(parameters);

        // uma consulta search_read (a outra chamada e o fields_get que filtra os campos do modelo)
        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                argThat((List<?> args) -> "search_read".equals(args.get(4)))
        );
    }

    @Test
    @DisplayName("Deve executar consultarSignalPorId com id, que resulta em um filtro com id, sucesso")
    void consultarSignalPorId() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);

        when(objectClient.execute(eq(EphemAdapter.EXECUTE_KW), anyList())).thenReturn(new Object[]{new HashMap<String, Object>() {{
            put("chave1", "valor1");
        }}});

        HashMap<String, Object> stringObjectHashMap = adapter.consultarSignalPorId(1L);

        assertEquals(stringObjectHashMap, new HashMap<String, Object>() {{
            put("chave1", "valor1");
        }});
        // uma consulta search_read (a outra chamada e o fields_get que filtra os campos do modelo)
        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                argThat((List<?> args) -> "search_read".equals(args.get(4)))
        );
    }

    @Test
    @DisplayName("Deve executar consultarSignalPorId com id, que resulta em um filtro com id, vazio")
    void consultarSignalPorIdRetornoVazio() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);

        when(objectClient.execute(eq(EphemAdapter.EXECUTE_KW), anyList())).thenReturn(new Object[]{});

        adapter.consultarSignalPorId(1L);

        // uma consulta search_read (a outra chamada e o fields_get que filtra os campos do modelo)
        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                argThat((List<?> args) -> "search_read".equals(args.get(4)))
        );
    }


    @Test
    @DisplayName("Deve executar listarRegistrosPorNomeModelo com erro")
    void listarRegistrosPorNomeModelo_ComErro() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var nomeModelo = "modelo";
        final var fields = List.of("field1", "field2");
        final var parameters = EphemParameters.builder()
                .nomeModelo(nomeModelo)
                .fields(fields)
                .build();
        when(objectClient.execute(anyString(), anyList())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> adapter.listarRegistros(parameters));

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        nomeModelo,
                        EphemAdapter.SEARCH_READ_REMOTE_PROCEDURE_NAME,
                        EphemAdapter.SEM_FILTRO,
                        Map.of(
                                EphemAdapter.FIELDS_PARAMETER_NAME, fields,
                                CONTEXT_PARAMETER_NAME, Map.ofEntries(
                                        entry(CONTEXT_LANG_KEY, "pt_BR")
                                ),
                                EphemAdapter.ORDER_BY_PARAMETER_NAME, "",
                                EphemAdapter.OFFSET_PARAMETER_NAME, 0,
                                EphemAdapter.LIMIT_PARAMETER_NAME, 10
                        )
                ))
        );
    }

    @Test
    @DisplayName("Deve executar recuperarVersaoInstanciaOdoo com sucesso")
    void recuperarVersaoInstanciaOdoo_CallsCommonClientWithCorrectParameters() throws Exception {
        when(commonClient.execute(anyString(), anyList())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> adapter.recuperarVersaoInstanciaOdoo());

        verify(commonClient, times(1)).execute(eq("version"), eq(emptyList()));
    }

    @Test
    @DisplayName("Deve executar recuperarVersaoInstanciaOdoo com erro")
    void recuperarVersaoInstanciaOdoo_ComErro() throws Exception {
        adapter.recuperarVersaoInstanciaOdoo();

        verify(commonClient, times(1)).execute(eq("version"), eq(emptyList()));
    }

    @Test
    @DisplayName("Deve executar listarModelos com sucesso")
    void listarModelos_CallsModelsClientWithCorrectParameters() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var modulesFiltro = "filtro_modulos";
        final var modelFiltro = "filtro_modelo";
        when(objectClient.execute(anyString(), anyList())).thenReturn(new Object[]{});

        adapter.listarModelos(modulesFiltro, modelFiltro);

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        "ir.model",
                        EphemAdapter.SEARCH_READ_REMOTE_PROCEDURE_NAME,
                        List.of(List.of(
                                List.of("modules", "ilike", modulesFiltro),
                                List.of("model", "ilike", modelFiltro)
                        )),
                        Map.of(
                                EphemAdapter.FIELDS_PARAMETER_NAME, List.of("id", EphemAdapter.MODEL_PARAMETER_NAME, "name", "modules"),
                                EphemAdapter.LIMIT_PARAMETER_NAME, 1000
                        )
                ))
        );
    }

    @Test
    @DisplayName("Deve executar listarModelos com erro")
    void listarModelos_ComErro() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var modulesFiltro = "filtro_modulos";
        final var modelFiltro = "filtro_modelo";
        when(objectClient.execute(anyString(), anyList())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> adapter.listarModelos(modulesFiltro, modelFiltro));

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        "ir.model",
                        EphemAdapter.SEARCH_READ_REMOTE_PROCEDURE_NAME,
                        List.of(List.of(
                                List.of("modules", "ilike", modulesFiltro),
                                List.of("model", "ilike", modelFiltro)
                        )),
                        Map.of(
                                EphemAdapter.FIELDS_PARAMETER_NAME, List.of("id", EphemAdapter.MODEL_PARAMETER_NAME, "name", "modules"),
                                EphemAdapter.LIMIT_PARAMETER_NAME, 1000
                        )
                ))
        );
    }

    @Test
    @DisplayName("Deve executar listarCamposModelo com sucesso")
    void listarCamposModelo_CallsModelsClientWithCorrectParameters() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var modeloNome = "nome_modelo";
        final var atributoNome = "nome_atributo";
        when(objectClient.execute(anyString(), anyList())).thenReturn(new Object[]{});

        adapter.listarCamposModelo(modeloNome, atributoNome);

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        "ir.model.fields",
                        EphemAdapter.SEARCH_READ_REMOTE_PROCEDURE_NAME,
                        List.of(List.of(
                                List.of("model", "=", modeloNome),
                                List.of("name", "=", atributoNome)
                        )),
                        Map.of(
                                EphemAdapter.FIELDS_PARAMETER_NAME, EphemAdapter.TODOS_OS_CAMPOS,
                                EphemAdapter.LIMIT_PARAMETER_NAME, 1000
                        )
                ))
        );
    }

    @Test
    @DisplayName("Deve executar listarCamposModelo com erro")
    void listarCamposModelo_ComoErro() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var modeloNome = "nome_modelo";
        final var atributoNome = "nome_atributo";
        when(objectClient.execute(anyString(), anyList())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> adapter.listarCamposModelo(modeloNome, atributoNome));

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        "ir.model.fields",
                        EphemAdapter.SEARCH_READ_REMOTE_PROCEDURE_NAME,
                        List.of(List.of(
                                List.of("model", "=", modeloNome),
                                List.of("name", "=", atributoNome)
                        )),
                        Map.of(
                                EphemAdapter.FIELDS_PARAMETER_NAME, EphemAdapter.TODOS_OS_CAMPOS,
                                EphemAdapter.LIMIT_PARAMETER_NAME, 1000
                        )
                ))
        );
    }

    @Test
    @DisplayName("Deve executar criarSignal com sucesso")
    void criarSignal_CallsModelsClientWithCorrectParameters() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        Map<String, Object> dados = Map.of("chave", "valor");
        when(objectClient.execute(anyString(), anyList())).thenReturn(1);

        adapter.criarSignal(createIntegrationEvent(), dados);

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        "eoc.signal",
                        "create",
                        List.of(dados)
                ))
        );
    }

    @Test
    @DisplayName("criarSignal tambem cria a mensagem padrao e a fonte (lider comunitario) com nome e telefone do usuario")
    void criarSignal_CriaMensagemEFonte() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var config = new br.unb.sds.gds2ephem.application.model.ConfiguracaoSistema();
        config.setCommunityLeadersSourceId(4L);
        when(configuracaoSistemaRepository.findById(br.unb.sds.gds2ephem.application.model.ConfiguracaoSistema.DEFAULT_SYSTEM_CONFIG_ID))
                .thenReturn(java.util.Optional.of(config));
        when(objectClient.execute(anyString(), anyList())).thenReturn(77, 88, 99);

        final var signalId = adapter.criarSignal(createIntegrationEvent(), Map.of("chave", "valor"));

        assertEquals(77L, signalId);
        verify(objectClient).execute(eq(EphemAdapter.EXECUTE_KW), eq(List.of(db, uid, odooApiKey, "mail.message", "create",
                List.of(Map.of(
                        "body", "Este registro foi gerado a partir do aplicativo Guardiões da Saúde",
                        "author_id", uid,
                        "message_type", "comment",
                        "model", "eoc.signal",
                        "res_id", 77L)))));
        verify(objectClient).execute(eq(EphemAdapter.EXECUTE_KW), eq(List.of(db, uid, odooApiKey, "eoc.signal.sources", "create",
                List.of(Map.of(
                        "signal_id", 77L,
                        "source_type", 4L,
                        "source_name", "example",
                        "source_phone", "99999999999999")))));
    }

    @Test
    @DisplayName("listarRegistros usa o idioma configurado (odoo.lang) quando a consulta nao define um")
    void listarRegistrosUsaIdiomaConfigurado() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        ReflectionTestUtils.setField(adapter, "odooLang", "pt_PT");
        when(objectClient.execute(anyString(), anyList())).thenReturn(new Object[]{});

        adapter.listarRegistros(EphemParameters.builder().nomeModelo("eoc.signal").build());

        final var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(objectClient).execute(eq(EphemAdapter.EXECUTE_KW), captor.capture());
        final var kwargs = (Map<?, ?>) captor.getValue().get(6);
        assertEquals(Map.of(CONTEXT_LANG_KEY, "pt_PT"), kwargs.get(CONTEXT_PARAMETER_NAME));
    }

    @Test
    @DisplayName("listarMensagens e contarMensagens usam o mesmo filtro em mail.message")
    void mensagensListagemEContagemUsamMesmoFiltro() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var parametros = EphemMessageParameters.builder()
                .signalIds(new Object[]{555L}).fromIds(new Object[]{7L, 3L}).partnerIds(new Object[]{7L, 3L})
                .limit(5L).offset(10L).build();
        final var mensagem = new java.util.HashMap<String, Object>();
        mensagem.put("id", 99);
        mensagem.put("body", "<p>oi</p>");
        when(objectClient.execute(eq(EphemAdapter.EXECUTE_KW), any(Object[].class)))
                .thenReturn(new Object[]{mensagem})
                .thenReturn(23);

        final var mensagens = adapter.listarMensagens(parametros);
        final var total = adapter.contarMensagens(parametros);

        assertEquals(1, mensagens.size());
        assertEquals(99, mensagens.get(0).getId());
        assertEquals(23L, total);
        final var captor = org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(objectClient, times(2)).execute(eq(EphemAdapter.EXECUTE_KW), captor.capture());
        final var listagem = captor.getAllValues().get(0);
        final var contagem = captor.getAllValues().get(1);
        assertEquals("search_read", listagem[4]);
        assertEquals("search_count", contagem[4]);
        org.assertj.core.api.Assertions.assertThat(contagem[5]).isEqualTo(listagem[5]);
        org.assertj.core.api.Assertions.assertThat((Object[]) ((Object[]) listagem[5])[0])
                .anySatisfy(condicao -> org.assertj.core.api.Assertions.assertThat((Object[]) condicao)
                        .containsExactly("res_id", "in", new Object[]{555L}));
    }

    @Test
    @DisplayName("criarSignal devolve o id mesmo se a fonte falhar (o signal nao pode ser recriado)")
    void criarSignal_FonteFalhaNaoRelanca() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        when(configuracaoSistemaRepository.findById(anyLong())).thenThrow(new RuntimeException("banco fora"));
        when(objectClient.execute(anyString(), anyList())).thenReturn(77, 88);

        assertEquals(77L, adapter.criarSignal(createIntegrationEvent(), Map.of("chave", "valor")));
    }

    @Test
    @DisplayName("Deve executar criarSignal com sucesso mas mensagem com erro")
    void criarSignal_MensagemErro() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        Map<String, Object> dados = Map.of("chave", "valor");
        when(objectClient.execute(anyString(), anyList())).thenReturn(1).thenThrow(new RuntimeException());

        adapter.criarSignal(createIntegrationEvent(), dados);

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        "eoc.signal",
                        "create",
                        List.of(dados)
                ))
        );
    }

    @Test
    @DisplayName("Deve executar criarSignal com erro")
    void criarSignal_LogsErrorWhenCreatingMensagemThrowsException() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        Map<String, Object> dados = Map.of("chave", "valor");
        doThrow(new RuntimeException()).when(objectClient).execute(anyString(), anyList());

        assertThrows(RuntimeException.class, () -> adapter.criarSignal(createIntegrationEvent(), dados));

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        "eoc.signal",
                        "create",
                        List.of(dados)
                ))
        );
    }

    @Test
    @DisplayName("Deve executar criarMensagem")
    void criarMensagem_CallsModelsClientWithCorrectParameters() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var signalId = 1L;
        final var mensagem = "mensagem";
        when(objectClient.execute(anyString(), anyList())).thenReturn(1).thenThrow(new RuntimeException());

        adapter.criarMensagem(signalId, mensagem);

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                eq(List.of(
                        db,
                        uid,
                        odooApiKey,
                        "mail.message",
                        "create",
                        List.of(
                                Map.of(
                                        "body", mensagem,
                                        "author_id", uid,
                                        "message_type", "comment",
                                        EphemAdapter.MODEL_PARAMETER_NAME, "eoc.signal",
                                        "res_id", signalId
                                )
                        )
                ))
        );
    }

    @Test
    @DisplayName("Deve executar deletarSignalPorId")
    void testDeletar() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var signalId = 1L;
        when(objectClient.execute(anyString(), anyList())).thenReturn(1);

        adapter.deletarSignalPorId(signalId);

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                anyList()
        );
    }


    @Test
    @DisplayName("Deve executar deletarSignalPorId erro")
    void testDeletarErro() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var signalId = 1L;
        when(objectClient.execute(anyString(), anyList())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> adapter.deletarSignalPorId(signalId));
    }

    @Test
    @DisplayName("listarRegistros pede so os campos que o modelo tem (ePHEM 18 nao tem country_id/state_id)")
    void listarRegistrosIgnoraCamposInexistentes() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        final var camposDoModelo = new HashMap<String, Object>();
        camposDoModelo.put("id", Map.of("type", "integer"));
        camposDoModelo.put("signal_stage_state_id", Map.of("type", "many2one"));
        camposDoModelo.put("country_ids", Map.of("type", "many2many"));
        when(objectClient.execute(eq(EphemAdapter.EXECUTE_KW), anyList())).thenAnswer(inv -> {
            final List<?> args = inv.getArgument(1);
            return "fields_get".equals(args.get(4)) ? camposDoModelo : new Object[]{};
        });
        final var parametros = EphemParameters.builder().nomeModelo("eoc.signal")
                .fields(List.of("id", "country_id", "state_id", "signal_stage_state_id", "__last_update")).build();

        adapter.listarRegistros(parametros);
        adapter.listarRegistros(parametros);

        final var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(objectClient, times(3)).execute(eq(EphemAdapter.EXECUTE_KW), captor.capture());
        final var chamadas = captor.getAllValues();
        // fields_get uma vez so (cache por modelo) e dois search_read
        assertEquals(1, chamadas.stream().filter(c -> "fields_get".equals(c.get(4))).count());
        final var buscas = chamadas.stream().filter(c -> "search_read".equals(c.get(4))).collect(java.util.stream.Collectors.toList());
        assertEquals(2, buscas.size());
        buscas.forEach(b -> assertEquals(List.of("id", "signal_stage_state_id"), ((Map<?, ?>) b.get(6)).get(EphemAdapter.FIELDS_PARAMETER_NAME)));
    }

    @Test
    @DisplayName("Se fields_get falhar, a consulta segue com os campos pedidos e tenta ler de novo depois")
    void listarRegistrosSemFieldsGet() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        when(objectClient.execute(eq(EphemAdapter.EXECUTE_KW), anyList())).thenAnswer(inv -> {
            final List<?> args = inv.getArgument(1);
            if ("fields_get".equals(args.get(4))) {
                throw new IllegalStateException("timeout");
            }
            return new Object[]{};
        });
        final var campos = List.of("id", "country_id");

        adapter.listarRegistros(EphemParameters.builder().nomeModelo("eoc.signal").fields(campos).build());
        adapter.listarRegistros(EphemParameters.builder().nomeModelo("eoc.signal").fields(campos).build());

        final var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(objectClient, times(4)).execute(eq(EphemAdapter.EXECUTE_KW), captor.capture());
        captor.getAllValues().stream().filter(c -> "search_read".equals(c.get(4)))
                .forEach(b -> assertEquals(campos, ((Map<?, ?>) b.get(6)).get(EphemAdapter.FIELDS_PARAMETER_NAME)));
    }
}
