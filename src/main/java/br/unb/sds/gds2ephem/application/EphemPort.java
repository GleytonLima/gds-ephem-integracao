package br.unb.sds.gds2ephem.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface EphemPort {
    void autenticar();

    Object listarAtributosPorNomeModelo(String modelName);

    List<Object> listarRegistrosPorNomeModelo(String nomeModelo, List<String> fields);

    List<Object> listarRegistrosPorNomeModeloComId(final String nomeModelo, final List<String> fields, final Long id);

    Object recuperarVersaoInstanciaOdoo();

    List<Object> listarModelos(String modulesFiltro, String modelFiltro);

    List<Object> listarCamposModelo(String modeloNome, String atributoNome);

    Long criarSignal(Map<String, Object> dados);

    Long criarMensagem(Long signalId, String mensagem);

    HashMap<String, Object> consultarSignalPorId(Long id);
}
