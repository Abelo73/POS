package com.novapos.inventory.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.UUID;

public record SubmitStockCountRequest(
        @NotEmpty List<CountedLineInput> lines
) {
    public record CountedLineInput(@NotNull UUID lineId, @PositiveOrZero int countedQuantity) {}
}
