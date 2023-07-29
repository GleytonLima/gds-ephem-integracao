package br.unb.sds.gds2ephem.httpcontroller.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@Data
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Signal {
    private Long eventId;
    private Long signalId;
    private HashMap<String, Object> dados;
}
