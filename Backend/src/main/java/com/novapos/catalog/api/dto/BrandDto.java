package com.novapos.catalog.api.dto;

import java.util.UUID;

public record BrandDto(
        UUID id,
        UUID companyId,
        String name
) {
}
