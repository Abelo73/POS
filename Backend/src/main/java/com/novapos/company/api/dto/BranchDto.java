package com.novapos.company.api.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record BranchDto(
        UUID id,
        UUID companyId,
        String name,
        String timezone,
        String taxZone,
        String currency,
        Map<String, Object> address,
        Map<String, Object> openingHours,
        Instant createdAt,
        Instant updatedAt
) {
}
