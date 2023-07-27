package br.unb.sds.gds2ephem.ephem;

import br.unb.sds.gds2ephem.application.EphemPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Slf4j
@Component
@Profile("local")
public class EphemNoopAdapter implements EphemPort {
    public EphemNoopAdapter() {
        this.autenticar();
    }

    public void autenticar() {
        log.info("Autenticando Noop");
    }

    public Object listarAtributosPorNomeModelo(final String modelName) {
        log.info("listarAtributosPorNomeModelo Noop");
        return new Object();
    }


    public List<Object> listarRegistrosPorNomeModelo(final String nomeModelo, final List<String> fields) {
        log.info("listarRegistrosPorNomeModelo Noop");
        return emptyList();
    }

    @Override
    public List<Object> listarRegistros(EphemParameters ephemParameters) {
        return emptyList();
    }

    public Object recuperarVersaoInstanciaOdoo() {
        log.info("recuperarVersaoInstanciaOdoo Noop");
        return new Object();
    }

    public List<Object> listarModelos(final String modulesFiltro, final String modelFiltro) {
        log.info("listarModelos Noop");
        return emptyList();
    }

    public List<Object> listarCamposModelo(final String modeloNome, final String atributoNome) {
        log.info("listarCamposModelo Noop");
        return emptyList();
    }

    public Long criarSignal(final Map<String, Object> dados) {
        log.info("criarSignal Noop: {}", dados);
        return 1L;
    }

    public Long criarMensagem(final Long signalId, final String mensagem) {
        log.info("criarMensagem Noop");
        return 1L;
    }

    @Override
    public HashMap<String, Object> consultarSignalPorId(Long id) {
        return new HashMap<>();
    }

    @Override
    public void deletarSignalPorId(Long signalId) {
        log.info("deletarSignalPorId Noop");
    }
}
