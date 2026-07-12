package com.novapos.user.api.dto;

import java.util.UUID;

public record RoleDto(
        UUID id,
        String name,
        String description
) {
}
