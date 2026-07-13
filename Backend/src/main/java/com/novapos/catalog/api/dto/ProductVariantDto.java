package com.novapos.catalog.api.dto;

import java.util.UUID;

public record ProductVariantDto(
        UUID id,
        UUID productId,
        String variantName,
        String barcode,
        Long priceOverrideMinor
) {
}
