package com.novapos.catalog.web.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record UpdateProductRequest(
        String sku,
        String name,
        @PositiveOrZero Long basePriceMinor,
        String currency,
        String taxClass,
        UUID categoryId,
        UUID brandId
) {
}
