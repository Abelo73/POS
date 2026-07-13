package com.novapos.catalog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CreateProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @NotNull @PositiveOrZero Long basePriceMinor,
        @NotBlank String currency,
        String taxClass,
        UUID categoryId,
        UUID brandId
) {
}
