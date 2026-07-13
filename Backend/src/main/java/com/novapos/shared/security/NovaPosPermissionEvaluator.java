package com.novapos.shared.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NovaPosPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        var role = permission.toString();
        return hasRole(auth, role);
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        var role = permission.toString();

        switch (targetType) {
            case "branch":
                return hasBranchScope(auth, targetId) && hasRole(auth, role);
            case "company":
                return hasCompanyScope(auth, targetId) && hasRole(auth, role);
            default:
                return hasRole(auth, role);
        }
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private boolean hasBranchScope(Authentication auth, Serializable targetId) {
        if (!(auth.getDetails() instanceof Claims claims)) {
            return false;
        }
        var roles = extractRoles(claims);
        return roles.stream().anyMatch(r -> {
            var branchId = r.get("branchId");
            if (branchId == null) {
                return false;
            }
            return branchId.toString().equals(targetId.toString());
        });
    }

    private boolean hasCompanyScope(Authentication auth, Serializable targetId) {
        if (!(auth.getDetails() instanceof Claims claims)) {
            return false;
        }
        var companyId = claims.get("companyId", String.class);
        return companyId != null && companyId.equals(targetId.toString());
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRoles(Claims claims) {
        var roles = claims.get("roles", List.class);
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .filter(Map.class::isInstance)
                .map(r -> (Map<String, Object>) r)
                .toList();
    }
}
