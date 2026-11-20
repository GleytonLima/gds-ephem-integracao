package br.unb.sds.gds2ephem.ephemv2;

import br.unb.sds.gds2ephem.ephem.NarrativeSignalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(EphemV2Properties.class)
@ConditionalOnProperty(name = "ephem.v2.enabled", havingValue = "true")
public class EphemV2Config {

    @Bean
    public EphemV2Client ephemV2Client(RestTemplateBuilder builder, EphemV2Properties properties, ObjectMapper objectMapper) {
        Assert.hasText(properties.getBaseUrl(), "ephem.v2.base-url (EPHEM_V2_BASE_URL) e obrigatorio quando ephem.v2.enabled=true");
        Assert.hasText(properties.getApiKey(), "ephem.v2.api-key (EPHEM_V2_API_KEY) e obrigatorio quando ephem.v2.enabled=true");
        final var restTemplate = builder
                .setConnectTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout())
                .build();
        return new EphemV2Client(restTemplate, properties, objectMapper);
    }

    @Bean
    public EphemV2SignalMapper ephemV2SignalMapper(EphemV2Properties properties, ObjectMapper objectMapper) {
        return new EphemV2SignalMapper(properties, objectMapper);
    }

    @Bean
    public EphemV2PlaceResolver ephemV2PlaceResolver(EphemV2Client client, EphemV2Properties properties) {
        return new EphemV2PlaceResolver(client, properties.getPlaceCacheTtl(), Clock.systemUTC());
    }

    @Bean
    public EphemV2SignalPublisher ephemV2SignalPublisher(EphemV2Client client, EphemV2SignalMapper mapper,
                                                         EphemV2PlaceResolver placeResolver,
                                                         NarrativeSignalService narrativeSignalService) {
        return new EphemV2SignalPublisher(client, mapper, placeResolver, narrativeSignalService);
    }
}
