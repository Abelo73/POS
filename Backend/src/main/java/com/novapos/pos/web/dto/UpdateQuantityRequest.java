package com.novapos.pos.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateQuantityRequest(
        @NotNull @Positive BigDecimal quantity
) {
}
