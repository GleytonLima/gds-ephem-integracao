package br.unb.sds.gds2ephem.httpcontroller.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Signal {
    private Long eventId;
    private Long signalId;
    private Map<String, Object> dados;

    public Signal(Long eventId, Long signalId, Map<String, Object> dados) {
        this.eventId = eventId;
        this.signalId = signalId;
        this.dados = dados;
        this.traduzirStatus();
    }

    public void traduzirStatus() {
        Map<String, String> substituicoes = new HashMap<>();
        substituicoes.put("verification", "Em verificação");
        substituicoes.put("raw information", "Inicial");
        substituicoes.put("discarded", "Descartado");
        substituicoes.put("monitoring", "Em monitoramento");
        substituicoes.put("event", "Evento");
        substituicoes.put("closed", "Fechado");
        substituicoes.put("invalid", "-");

        for (Map.Entry<String, Object> entry : dados.entrySet()) {
            Object valor = entry.getValue();
            if (valor instanceof Object[] && ((Object[]) valor).length >= 2) {
                Object[] arrayValor = (Object[]) valor;
                if (arrayValor[1] instanceof String) {
                    String palavraChave = (String) arrayValor[1];
                    if (substituicoes.containsKey(palavraChave.toLowerCase())) {
                        arrayValor[1] = substituicoes.get(palavraChave.toLowerCase());
                    }
                }
            }
        }
    }
}
