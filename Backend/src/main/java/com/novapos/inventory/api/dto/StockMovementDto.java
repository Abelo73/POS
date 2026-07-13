package com.novapos.inventory.api.dto;

import java.time.Instant;
import java.util.UUID;

public record StockMovementDto(
        UUID id,
        UUID productVariantId,
        UUID locationId,
        int quantityDelta,
        String reason,
        Long unitCostMinor,
        UUID batchId,
        String referenceType,
        UUID referenceId,
        Instant createdAt,
        UUID createdBy
) {
}
