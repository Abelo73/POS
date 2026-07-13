package com.novapos.inventory.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public record ReceiptRequest(
        @NotNull UUID productVariantId,
        @NotNull UUID locationId,
        @Positive int quantity,
        @NotNull Long unitCostMinor,
        String batchCode,
        LocalDate expiryDate
) {
}
