package com.novapos.inventory.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.UUID;

public record CreateStockCountRequest(
        @NotNull UUID locationId,
        @NotEmpty List<StockCountLineInput> lines,
        @PositiveOrZero long varianceThreshold
) {
    public record StockCountLineInput(@NotNull UUID productVariantId, @PositiveOrZero int expectedQuantity) {}
}
