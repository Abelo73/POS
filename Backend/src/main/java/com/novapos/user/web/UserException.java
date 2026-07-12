package com.novapos.user.web;

import com.novapos.shared.exception.NovaPosException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserException extends NovaPosException {

    public UserException(String code, String message, HttpStatus status) {
        super(code, message, status);
    }

    public static UserException notFound(UUID userId) {
        return new UserException("USER_NOT_FOUND", "User not found: " + userId, HttpStatus.NOT_FOUND);
    }

    public static UserException emailAlreadyExists(String email) {
        return new UserException("EMAIL_EXISTS", "A user with email " + email + " already exists.", HttpStatus.CONFLICT);
    }

    public static UserException roleNotFound(String roleName) {
        return new UserException("ROLE_NOT_FOUND", "Role not found: " + roleName, HttpStatus.NOT_FOUND);
    }
}
