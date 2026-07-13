package com.novapos.pos.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateReturnRequest(
        @NotEmpty List<ReturnItem> items
) {
    public record ReturnItem(
            @NotNull UUID saleLineId,
            @NotNull @Positive BigDecimal quantity,
            @NotBlank String refundMethod
    ) {}
}
