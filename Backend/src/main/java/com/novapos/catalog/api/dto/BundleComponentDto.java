package com.novapos.catalog.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BundleComponentDto(
        UUID id,
        UUID bundleProductId,
        UUID componentProductId,
        BigDecimal quantity
) {
}
