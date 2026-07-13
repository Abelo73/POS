package com.novapos.purchasing.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreatePORequest(@NotNull UUID supplierId, @NotNull UUID branchId, @NotEmpty List<POLineInput> lines) {
    public record POLineInput(@NotNull UUID productVariantId, @NotNull @Positive BigDecimal quantityOrdered, @NotNull Long unitCostMinor) {}
}
