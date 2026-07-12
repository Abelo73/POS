package com.novapos.company.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateBranchRequest(
        String name,
        String timezone,
        String taxZone,
        @Size(min = 3, max = 3) String currency
) {
}
