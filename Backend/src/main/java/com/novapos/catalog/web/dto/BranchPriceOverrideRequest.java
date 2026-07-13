package com.novapos.catalog.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record BranchPriceOverrideRequest(
        @NotNull @PositiveOrZero Long priceMinor
) {
}
