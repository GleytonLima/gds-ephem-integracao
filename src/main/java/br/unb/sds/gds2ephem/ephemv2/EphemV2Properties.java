package br.unb.sds.gds2ephem.ephemv2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuracao da API REST v2 do ePHEM (modulo ephem_api_base).
 * Ver "ePHEM API v2 Integrator Guide".
 */
@Data
@ConfigurationProperties(prefix = "ephem.v2")
public class EphemV2Properties {
    /**
     * Quando true, os signals sao publicados pela API REST v2 em vez do XML-RPC.
     */
    private boolean enabled = false;
    /**
     * Ex.: https://cv.pheoc.com/api/v2
     */
    private String baseUrl;
    /**
     * API key do usuario tecnico (Authorization: Bearer).
     */
    private String apiKey;
    /**
     * Prefixo do external_id. O external_id final e prefixo + id do evento_integracao,
     * o que torna o POST idempotente em caso de reprocessamento.
     */
    private String externalIdPrefix = "GDS-";
    /**
     * Campo do evento (data) usado como titulo do signal (maximo 50 caracteres).
     */
    private String titleField = "evento_descricao";
    /**
     * Campo do evento (data) com a data de ocorrencia, enviada como onset_date.
     */
    private String onsetDateField = "evento_data_ocorrencia";
    /**
     * Campo do evento (data) que indica quem foi afetado (pessoas, animais, ambiente).
     */
    private String healthInterfaceField = "evento_afetados";
    /**
     * Endereco enviado em source.address quando o evento nao tem telefone nem email.
     */
    private String sourceAddress = "https://gds.proepi.org.br";
    private int confidentiality = 1;
    private String privacy = "portal";
    private int maxRetries = 3;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);
    /**
     * Teto de espera quando o servidor responde 429 com Retry-After.
     */
    private Duration maxRetryAfter = Duration.ofSeconds(30);
    /**
     * Por quanto tempo as listas de estados e municipios ficam em cache (por pais).
     */
    private Duration placeCacheTtl = Duration.ofHours(6);
}
