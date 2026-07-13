package com.novapos.inventory.api.dto;

import java.util.UUID;

public record TransferLineDto(
        UUID id,
        UUID transferOrderId,
        UUID productVariantId,
        int quantity
) {
}
