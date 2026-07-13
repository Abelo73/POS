package com.novapos.inventory.api.dto;

import java.util.UUID;

public record ReorderConfigDto(
        UUID id,
        UUID productVariantId,
        UUID locationId,
        int reorderLevel,
        int reorderQuantity
) {
}
