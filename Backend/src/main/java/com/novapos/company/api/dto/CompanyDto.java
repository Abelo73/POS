package com.novapos.company.api.dto;

import java.time.Instant;
import java.util.UUID;

public record CompanyDto(
        UUID id,
        String name,
        String defaultCurrency,
        String billingStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
