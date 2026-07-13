package com.novapos.catalog.api.dto;

import java.util.UUID;

public record BranchPriceOverrideDto(
        UUID id,
        UUID productId,
        UUID branchId,
        long priceMinor
) {
}
