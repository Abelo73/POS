package com.novapos.shared.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NovaPosPermissionEvaluatorTest {

    private static final SecretKey KEY = Keys.hmacShaKeyFor("test-key-for-unit-tests-32bytes!!".getBytes(StandardCharsets.UTF_8));
    private final NovaPosPermissionEvaluator evaluator = new NovaPosPermissionEvaluator();

    @Test
    @DisplayName("Cashier role is denied access to BRANCH_MANAGER-required endpoint")
    void cashierDeniedForBranchManagerEndpoint() {
        var auth = createAuth("CASHIER", UUID.randomUUID(), UUID.randomUUID());

        var result = evaluator.hasPermission(auth, null, "BRANCH_MANAGER");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Branch Manager is granted access to BRANCH_MANAGER-required endpoint")
    void branchManagerGrantedForBranchManagerEndpoint() {
        var auth = createAuth("BRANCH_MANAGER", UUID.randomUUID(), UUID.randomUUID());

        var result = evaluator.hasPermission(auth, null, "BRANCH_MANAGER");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Branch Manager from Branch A is denied access to Branch B's data (scope check)")
    void branchManagerDeniedForOtherBranch() {
        var branchA = UUID.randomUUID();
        var branchB = UUID.randomUUID();
        var company = UUID.randomUUID();
        var auth = createAuth("BRANCH_MANAGER", company, branchA);

        var result = evaluator.hasPermission(auth, branchB, "branch", "BRANCH_MANAGER");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Branch Manager from Branch A is granted access to Branch A's data (scope check)")
    void branchManagerGrantedForOwnBranch() {
        var branchA = UUID.randomUUID();
        var company = UUID.randomUUID();
        var auth = createAuth("BRANCH_MANAGER", company, branchA);

        var result = evaluator.hasPermission(auth, branchA, "branch", "BRANCH_MANAGER");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Unauthenticated user is denied")
    void unauthenticatedDenied() {
        var auth = new UsernamePasswordAuthenticationToken("anon", null, List.of());

        var result = evaluator.hasPermission(auth, UUID.randomUUID(), "branch", "BRANCH_MANAGER");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Null authentication is denied")
    void nullAuthenticationDenied() {
        var result = evaluator.hasPermission(null, UUID.randomUUID(), "branch", "BRANCH_MANAGER");

        assertThat(result).isFalse();
    }

    private UsernamePasswordAuthenticationToken createAuth(String roleName, UUID companyId, UUID branchId) {
        var userId = UUID.randomUUID().toString();
        var now = new Date();

        var roles = List.of(Map.of(
                "roleId", UUID.randomUUID().toString(),
                "roleName", roleName,
                "branchId", branchId.toString()
        ));

        var token = Jwts.builder()
                .subject(userId)
                .claim("email", "user@test.com")
                .claim("companyId", companyId.toString())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + 3600_000))
                .signWith(KEY)
                .compact();

        var claims = Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));

        var auth = new UsernamePasswordAuthenticationToken(userId, token, authorities);
        auth.setDetails(claims);
        return auth;
    }
}
