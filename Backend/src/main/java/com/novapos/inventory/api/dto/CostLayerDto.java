package com.novapos.inventory.api.dto;

import java.util.UUID;

public record CostLayerDto(
        UUID id,
        UUID productVariantId,
        UUID locationId,
        int quantity,
        long unitCostMinor
) {
}
