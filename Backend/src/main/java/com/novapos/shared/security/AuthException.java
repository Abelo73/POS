package com.novapos.shared.security;

import com.novapos.shared.exception.NovaPosException;
import org.springframework.http.HttpStatus;

public class AuthException extends NovaPosException {

    public AuthException(String code, String message, HttpStatus status) {
        super(code, message, status);
    }

    public static AuthException invalidCredentials() {
        return new AuthException("INVALID_CREDENTIALS", "Invalid email or password.", HttpStatus.UNAUTHORIZED);
    }

    public static AuthException invalidToken() {
        return new AuthException("INVALID_TOKEN", "Token is invalid or expired.", HttpStatus.UNAUTHORIZED);
    }

    public static AuthException userNotActive() {
        return new AuthException("USER_NOT_ACTIVE", "This user account is deactivated.", HttpStatus.UNAUTHORIZED);
    }

    public static AuthException mfaRequired() {
        return new AuthException("MFA_REQUIRED", "MFA verification required for this account.", HttpStatus.UNAUTHORIZED);
    }

    public static AuthException invalidMfaCode() {
        return new AuthException("INVALID_MFA_CODE", "Invalid MFA verification code.", HttpStatus.UNAUTHORIZED);
    }
}
