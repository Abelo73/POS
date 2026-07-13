package com.novapos.pos.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSaleRequest(
        @NotNull UUID branchId,
        @NotNull UUID cashierId,
        UUID customerId,
        @NotBlank String currency,
        @NotNull UUID clientUuid
) {
}
