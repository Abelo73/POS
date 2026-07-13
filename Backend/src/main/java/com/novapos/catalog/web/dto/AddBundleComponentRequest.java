package com.novapos.catalog.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record AddBundleComponentRequest(
        @NotNull UUID componentProductId,
        @NotNull @Positive BigDecimal quantity
) {
}
