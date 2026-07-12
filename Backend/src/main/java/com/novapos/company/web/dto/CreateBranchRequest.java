package com.novapos.company.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateBranchRequest(
        @NotBlank String name,
        @NotBlank String timezone,
        @NotBlank String taxZone,
        @NotBlank @Size(min = 3, max = 3) String currency,
        Map<String, Object> address,
        Map<String, Object> openingHours
) {
}
