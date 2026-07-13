package com.novapos.catalog.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateVariantRequest(
        @NotBlank String variantName,
        String barcode,
        Long priceOverrideMinor
) {
}
