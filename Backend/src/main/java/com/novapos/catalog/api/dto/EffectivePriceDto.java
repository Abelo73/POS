package com.novapos.catalog.api.dto;

import java.util.UUID;

public record EffectivePriceDto(
        UUID productVariantId,
        UUID productId,
        long priceMinor,
        String currency,
        String source
) {
}
