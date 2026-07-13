package com.novapos.inventory.api.dto;

import java.util.UUID;

public record StockCountLineDto(
        UUID id,
        UUID stockCountId,
        UUID productVariantId,
        int expectedQuantity,
        Integer countedQuantity
) {
}
