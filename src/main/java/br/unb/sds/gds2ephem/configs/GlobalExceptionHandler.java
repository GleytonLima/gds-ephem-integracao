package br.unb.sds.gds2ephem.configs;

import br.unb.sds.gds2ephem.application.model.exceptions.EphemAuthException;
import br.unb.sds.gds2ephem.httpcontroller.dto.ApiError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @Value("${odoo.url}")
    private String url;

    @ExceptionHandler(EphemAuthException.class)
    public ResponseEntity<ApiError> handleEphemAuthException(EphemAuthException ex) {
        final var errorMessage = "ocorreu um erro de autenticação com o Ephem" + ex.getMessage();

        final var apiError = new ApiError(
                "https://developer.mozilla.org/pt-BR/docs/Web/HTTP/Status/401",
                "erro de autenticação no url: " + url,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                errorMessage);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
