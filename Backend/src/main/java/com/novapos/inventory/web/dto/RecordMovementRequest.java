package com.novapos.inventory.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RecordMovementRequest(
        @NotNull UUID productVariantId,
        @NotNull UUID locationId,
        int quantityDelta,
        @NotNull String reason,
        Long unitCostMinor,
        UUID batchId,
        String referenceType,
        UUID referenceId,
        UUID createdBy
) {
}
