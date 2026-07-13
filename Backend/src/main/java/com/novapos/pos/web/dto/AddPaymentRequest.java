package com.novapos.pos.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AddPaymentRequest(
        @NotBlank String method,
        @NotNull @PositiveOrZero Long amountMinor,
        String reference
) {
}
