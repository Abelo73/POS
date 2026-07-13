package com.novapos.catalog.web.dto;

public record UpdateVariantRequest(
        String variantName,
        String barcode,
        Long priceOverrideMinor
) {
}
