package com.novapos.inventory.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record CreateTransferRequest(
        @NotNull UUID sourceLocationId,
        @NotNull UUID destinationLocationId,
        @NotEmpty List<TransferLineInput> lines
) {
    public record TransferLineInput(@NotNull UUID productVariantId, @Positive int quantity) {}
}
