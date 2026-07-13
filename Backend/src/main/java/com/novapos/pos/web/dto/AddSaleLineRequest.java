package com.novapos.pos.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record AddSaleLineRequest(
        @NotNull UUID productVariantId,
        @NotNull @Positive BigDecimal quantity,
        @NotNull Long unitPriceMinor
) {
}
