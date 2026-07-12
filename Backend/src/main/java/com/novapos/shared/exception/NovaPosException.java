package com.novapos.shared.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Objects;

public abstract class NovaPosException extends RuntimeException {

    private final String code;
    private final transient Map<String, Object> details;
    private final HttpStatus status;

    protected NovaPosException(String code, String message, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.details = details != null ? Map.copyOf(details) : Map.of();
    }

    protected NovaPosException(String code, String message, HttpStatus status) {
        this(code, message, status, null);
    }

    public String getCode() {
        return code;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
