package com.novapos.inventory.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBatchRequest(
        @NotNull UUID productVariantId,
        @NotBlank String batchCode,
        LocalDate expiryDate
) {
}
