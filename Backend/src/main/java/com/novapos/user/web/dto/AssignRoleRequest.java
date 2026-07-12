package com.novapos.user.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AssignRoleRequest(
        @NotBlank String roleName,
        UUID branchId
) {
}
