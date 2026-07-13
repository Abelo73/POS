package com.novapos.inventory.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record BatchDto(
        UUID id,
        UUID productVariantId,
        String batchCode,
        LocalDate expiryDate
) {
}
