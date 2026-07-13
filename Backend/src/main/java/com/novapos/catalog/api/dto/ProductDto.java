package com.novapos.catalog.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ProductDto(
        UUID id,
        UUID companyId,
        String sku,
        String name,
        UUID categoryId,
        UUID brandId,
        long basePriceMinor,
        String currency,
        String taxClass,
        boolean trackInventory,
        String costingMethod,
        boolean isComposite,
        boolean soldByWeight,
        Instant createdAt,
        Instant updatedAt
) {
}
