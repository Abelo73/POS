package com.novapos.inventory.api.dto;

import java.util.UUID;

public record BranchInventoryConfigDto(
        UUID id,
        UUID branchId,
        boolean allowNegativeStock
) {
}
