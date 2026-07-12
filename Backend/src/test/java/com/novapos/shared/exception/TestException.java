package com.novapos.shared.exception;

import org.springframework.http.HttpStatus;

public class TestException extends NovaPosException {

    public TestException(String code, String message, HttpStatus status) {
        super(code, message, status);
    }
}
