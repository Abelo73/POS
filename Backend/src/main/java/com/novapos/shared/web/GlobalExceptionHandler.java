package com.novapos.shared.web;

import com.novapos.shared.exception.NovaPosException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NovaPosException.class)
    public ResponseEntity<ErrorResponse> handleNovaPosException(NovaPosException ex, HttpServletRequest request) {
        ErrorResponse error = buildError(ex.getCode(), ex.getMessage(), ex.getDetails());
        logNovaPosException(ex, error.traceId(), request);
        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception ex, HttpServletRequest request) {
        String traceId = generateTraceId();
        log.error("Unhandled exception traceId={} method={} path={}", traceId, request.getMethod(), request.getRequestURI(), ex);
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again or contact support.",
                Map.of(),
                traceId,
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private void logNovaPosException(NovaPosException ex, String traceId, HttpServletRequest request) {
        if (ex.getStatus().is5xxServerError()) {
            log.error("NovaPosException traceId={} code={} method={} path={}", traceId, ex.getCode(), request.getMethod(), request.getRequestURI(), ex);
        } else {
            log.warn("NovaPosException traceId={} code={} status={} method={} path={}", traceId, ex.getCode(), ex.getStatus().value(), request.getMethod(), request.getRequestURI());
        }
    }

    private ErrorResponse buildError(String code, String message, Map<String, Object> details) {
        return new ErrorResponse(code, message, details, generateTraceId(), Instant.now());
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
