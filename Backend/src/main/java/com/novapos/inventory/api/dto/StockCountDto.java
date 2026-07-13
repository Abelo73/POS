package com.novapos.inventory.api.dto;

import java.util.List;
import java.util.UUID;

public record StockCountDto(
        UUID id,
        UUID locationId,
        String status,
        long varianceThreshold,
        List<StockCountLineDto> lines
) {
}
