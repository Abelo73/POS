package com.novapos.catalog.api.dto;

import java.util.UUID;

public record CategoryDto(
        UUID id,
        UUID companyId,
        UUID parentId,
        String name
) {
}
