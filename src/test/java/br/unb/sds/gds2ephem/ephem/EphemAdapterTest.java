package br.unb.sds.gds2ephem.ephem;

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
import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        adapter = new EphemAdapter(objectClient, commonClient);
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

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                anyList()
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
        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                anyList()
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

        verify(objectClient, times(1)).execute(
                eq(EphemAdapter.EXECUTE_KW),
                anyList()
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

        adapter.criarSignal(dados);

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
    @DisplayName("Deve executar criarSignal com sucesso mas mensagem com erro")
    void criarSignal_MensagemErro() throws Exception {
        ReflectionTestUtils.setField(adapter, "uid", uid);
        ReflectionTestUtils.setField(adapter, "db", db);
        ReflectionTestUtils.setField(adapter, "odooApiKey", odooApiKey);
        Map<String, Object> dados = Map.of("chave", "valor");
        when(objectClient.execute(anyString(), anyList())).thenReturn(1).thenThrow(new RuntimeException());

        adapter.criarSignal(dados);

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

        assertThrows(RuntimeException.class, () -> adapter.criarSignal(dados));

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
}
