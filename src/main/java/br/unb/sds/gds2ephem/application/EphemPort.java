package br.unb.sds.gds2ephem.application;

import br.unb.sds.gds2ephem.application.model.EventoIntegracao;
import br.unb.sds.gds2ephem.application.model.SignalSource;
import br.unb.sds.gds2ephem.application.model.SinalMensagem;
import br.unb.sds.gds2ephem.ephem.EphemMessageParameters;
import br.unb.sds.gds2ephem.ephem.EphemParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface EphemPort {
    void autenticar();

    Object listarAtributosPorNomeModelo(String modelName);

    List<Object> listarRegistros(EphemParameters ephemParameters);

    Object recuperarVersaoInstanciaOdoo();

    List<Object> listarModelos(String modulesFiltro, String modelFiltro);

    List<Object> listarCamposModelo(String modeloNome, String atributoNome);

    Long criarSignal(EventoIntegracao eventoIntegracao, Map<String, Object> dados);

    Long criarMensagem(Long signalId, String mensagem);

    Long addSource(SignalSource signalSource);

    Long criarMensagem(Long signalId, Long userId, String mensagem, Long partnerId);

    HashMap<String, Object> consultarSignalPorId(Long id);

    void deletarSignalPorId(Long signalId);

    List<SinalMensagem> listarMensagens(EphemMessageParameters ephemMessageParameters);
}
