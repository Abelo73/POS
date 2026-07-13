package com.novapos.pos.api.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentDto(
        UUID id,
        UUID saleId,
        String method,
        long amountMinor,
        String reference,
        Instant createdAt
) {
}
