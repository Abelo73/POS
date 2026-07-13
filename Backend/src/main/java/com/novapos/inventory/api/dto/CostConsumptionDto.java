package com.novapos.inventory.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CostConsumptionDto(
        UUID productVariantId,
        UUID locationId,
        int totalQuantity,
        long totalCostMinor,
        String method,
        List<CostLayerDto> consumedLayers
) {
}
