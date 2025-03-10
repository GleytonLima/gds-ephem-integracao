package br.unb.sds.gds2ephem.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotifyEventsService implements NotificationService {
    // O URL completo do canal, conforme fornecido pelo Notify.events
    @Value("${notifyevents.channel.url}")
    private String channelUrl;

    private final RestTemplate restTemplate;

    public NotifyEventsService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public void notificarErro(String titulo, String mensagem, Throwable erro) {
        try {
            // Criar um MultiValueMap para os dados do formulário (form-data)
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("title", titulo);
            formData.add("text", criarCorpoDoAlerta(mensagem, erro));
            formData.add("priority", "highest");
            formData.add("level", "error");

            // Configurar os headers para multipart/form-data
            final var headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Criar a entidade HTTP com os headers e o corpo
            final var requestEntity = new HttpEntity<>(formData, headers);

            // Enviar a requisição POST para o URL do canal
            final var response = restTemplate.postForEntity(
                    channelUrl,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Alerta enviado via Notify.events com sucesso. Response: {}", response.getBody());
            } else {
                log.warn("Notify.events retornou status não esperado: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Falha ao enviar alerta via Notify.events", e);
        }
    }

    private String criarCorpoDoAlerta(String mensagem, Throwable erro) {
        StringBuilder corpo = new StringBuilder();

        // Data e hora atual formatada
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        corpo.append("## Detalhes do Erro\n\n");
        corpo.append(mensagem).append("\n\n");
        corpo.append("**Horário:** ").append(dataHora).append("\n");
        corpo.append("**Erro:** ").append(erro.getMessage()).append("\n");
        corpo.append("**Tipo:** ").append(erro.getClass().getName()).append("\n\n");

        // Adicionar stack trace
        corpo.append("### Stack trace\n");
        corpo.append("```java\n");
        StackTraceElement[] stackTrace = erro.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            int linhasTrace = Math.min(12, stackTrace.length);
            for (int i = 0; i < linhasTrace; i++) {
                corpo.append(stackTrace[i].toString()).append("\n");
            }
            if (stackTrace.length > linhasTrace) {
                corpo.append("... mais " + (stackTrace.length - linhasTrace) + " linhas omitidas ...");
            }
        }
        corpo.append("\n```\n\n");

        // Adicionar informações do ambiente
        corpo.append("### Informações do Ambiente\n\n");

        try {
            corpo.append("**Hostname:** ").append(java.net.InetAddress.getLocalHost().getHostName()).append("\n");
        } catch (Exception e) {
            corpo.append("**Hostname:** Não disponível\n");
        }

        corpo.append("**Sistema:** ").append(System.getProperty("os.name")).append(" ");
        corpo.append(System.getProperty("os.version")).append("\n");
        corpo.append("**Java:** ").append(System.getProperty("java.version")).append("\n");

        return corpo.toString();
    }
}