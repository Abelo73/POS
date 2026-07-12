package com.novapos.user.api.dto;

import java.util.UUID;

public record RoleAssignmentDto(
        UUID roleId,
        String roleName,
        UUID branchId
) {
}
