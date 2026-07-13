package com.novapos.pos.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SaleDto(
        UUID id,
        UUID branchId,
        UUID customerId,
        UUID cashierId,
        String status,
        long subtotalMinor,
        long discountMinor,
        long taxMinor,
        long totalMinor,
        String currency,
        UUID clientUuid,
        Instant completedAt,
        Instant createdAt,
        List<SaleLineDto> lines,
        List<PaymentDto> payments
) {
}
