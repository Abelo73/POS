package com.novapos.catalog.api.dto;

import java.util.List;
import java.util.UUID;

public record BundleBreakdownDto(
        UUID bundleProductId,
        String bundleSku,
        String bundleName,
        List<BundleComponentDto> components
) {
}
