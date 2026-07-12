package com.novapos.user.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AppUserDto(
        UUID id,
        UUID companyId,
        String email,
        String phone,
        boolean mfaEnabled,
        boolean isActive,
        List<RoleAssignmentDto> roles,
        Instant createdAt
) {
}
