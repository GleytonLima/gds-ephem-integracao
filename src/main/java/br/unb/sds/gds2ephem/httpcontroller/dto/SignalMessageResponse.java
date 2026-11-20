package br.unb.sds.gds2ephem.httpcontroller.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SignalMessageResponse {
    /**
     * Id da mail.message criada no ePHEM. Com ele o GDS reconhece a propria mensagem quando ela volta na listagem,
     * mesmo que no ePHEM o corpo tenha o cabecalho com o autor.
     */
    private Long id;
    private String message;
}
