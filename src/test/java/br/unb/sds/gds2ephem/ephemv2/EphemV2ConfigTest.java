package br.unb.sds.gds2ephem.ephemv2;

import br.unb.sds.gds2ephem.application.SignalPublisher;
import br.unb.sds.gds2ephem.ephem.NarrativeSignalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class EphemV2ConfigTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(EphemV2Config.class)
            .withBean(RestTemplateBuilder.class, RestTemplateBuilder::new)
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withBean(NarrativeSignalService.class, NarrativeSignalService::new);

    @Test
    @DisplayName("Desabilitada por padrao: nenhum bean da v2")
    void desabilitada() {
        runner.run(context -> assertThat(context).doesNotHaveBean(EphemV2SignalPublisher.class));
    }

    @Test
    @DisplayName("Habilitada: o SignalPublisher e o da v2")
    void habilitada() {
        runner.withPropertyValues("ephem.v2.enabled=true", "ephem.v2.base-url=https://ephem.test/api/v2", "ephem.v2.api-key=k")
                .run(context -> assertThat(context).getBean(SignalPublisher.class).isInstanceOf(EphemV2SignalPublisher.class));
    }

    @Test
    @DisplayName("Habilitada sem api-key: falha ao subir, em vez de falhar a cada evento")
    void semApiKey() {
        runner.withPropertyValues("ephem.v2.enabled=true", "ephem.v2.base-url=https://ephem.test/api/v2")
                .run(context -> assertThat(context).hasFailed());
    }
}
