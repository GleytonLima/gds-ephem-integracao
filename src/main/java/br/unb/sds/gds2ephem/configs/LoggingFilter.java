package br.unb.sds.gds2ephem.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final var requestWrapper = new ContentCachingRequestWrapper(request);
        final var responseWrapper = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(requestWrapper, responseWrapper);

        final var requestBody = getStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
        final var responseBody = getStringValue(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());

        log.info("Incoming request body: " + requestBody);
        log.info("Outgoing response body: " + responseBody);

        responseWrapper.copyBodyToResponse();
    }

    private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding);
        } catch (UnsupportedEncodingException e) {
            log.error("Error parsing request/response body", e);
        }
        return "";
    }
}
