package com.novapos.inventory.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ConsumeRequest(
        @NotNull UUID productVariantId,
        @NotNull UUID locationId,
        @Positive int quantity
) {
}
