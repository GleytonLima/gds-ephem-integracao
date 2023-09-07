package br.unb.sds.gds2ephem.ephem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EphemNoopAdapterTest {

    private EphemNoopAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EphemNoopAdapter();
    }

    @Test
    void autenticar_LogsAutenticacao() {
        assertDoesNotThrow(() -> adapter.autenticar());
    }

    @Test
    void listarAtributosPorNomeModelo_LogsListagemAtributos() {
        final var modelName = "modelo";

        assertDoesNotThrow(() -> adapter.listarAtributosPorNomeModelo(modelName));
    }

    @Test
    void listarRegistrosPorNomeModelo_LogsListagemRegistros() {
        final var nomeModelo = "modelo";
        final var fields = Collections.singletonList("field");

        assertDoesNotThrow(() -> adapter.listarRegistrosPorNomeModelo(nomeModelo, fields));
    }

    @Test
    void recuperarVersaoInstanciaOdoo_LogsRecuperacaoVersao() {
        assertDoesNotThrow(() -> adapter.recuperarVersaoInstanciaOdoo());
    }

    @Test
    void listarModelos_LogsListagemModelos() {
        final var modulesFiltro = "filtro_modulos";
        final var modelFiltro = "filtro_modelo";

        assertDoesNotThrow(() -> adapter.listarModelos(modulesFiltro, modelFiltro));
    }

    @Test
    void listarCamposModelo_LogsListagemCamposModelo() {
        final var modeloNome = "nome_modelo";
        final var atributoNome = "nome_atributo";

        assertDoesNotThrow(() -> adapter.listarCamposModelo(modeloNome, atributoNome));
    }

    @Test
    void criarSignal_LogsCriacaoSignal() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("chave", "valor");

        assertDoesNotThrow(() -> adapter.criarSignal(dados));
    }

    @Test
    void criarMensagem_LogsCriacaoMensagem() {
        final var signalId = 123L;
        final var mensagem = "mensagem";

        assertDoesNotThrow(() -> adapter.criarMensagem(signalId, mensagem));
    }

    @Test
    void listarRegistrosPorNomeModeloComId() {
        final var parameters = EphemParameters.builder()
                .nomeModelo("model")
                .fields(emptyList())
                .id(1L)
                .build();
        assertDoesNotThrow(() -> adapter.listarRegistros(EphemParameters.builder().build()));
    }

    @Test
    void consultarSignalPorId() {
        assertDoesNotThrow(() -> adapter.consultarSignalPorId(1L));
    }

    @Test
    void deletarSignalPorId() {
        assertDoesNotThrow(() -> adapter.deletarSignalPorId(1L));
    }
}