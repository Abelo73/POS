package com.novapos.pos.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleLineDto(
        UUID id,
        UUID saleId,
        UUID productVariantId,
        BigDecimal quantity,
        long unitPriceMinor,
        long discountMinor,
        long taxMinor
) {
}
