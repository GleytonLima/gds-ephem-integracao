package br.unb.sds.gds2ephem.httpcontroller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Data
public class ApiError {
    private String type;
    private String title;
    private int status;
    private String detail;
}