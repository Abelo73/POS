package com.novapos.user.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        var tokens = authService.login(request.email(), request.password(), request.totpCode());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    ResponseEntity<Map<String, String>> refresh(@Valid @RequestBody RefreshRequest request) {
        var tokens = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/pin-login")
    ResponseEntity<Map<String, String>> pinLogin(@Valid @RequestBody PinLoginRequest request) {
        var tokens = authService.pinLogin(request.terminalId(), request.pinCode());
        return ResponseEntity.ok(tokens);
    }

    record LoginRequest(@NotBlank String email, @NotBlank String password, String totpCode) {}

    record RefreshRequest(@NotBlank String refreshToken) {}

    record PinLoginRequest(@jakarta.validation.constraints.NotNull UUID terminalId, @NotBlank String pinCode) {}
}
