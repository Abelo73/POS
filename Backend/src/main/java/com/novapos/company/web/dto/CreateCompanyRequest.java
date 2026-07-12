package com.novapos.company.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank String name,
        @NotBlank @Size(min = 3, max = 3) String defaultCurrency,
        @NotBlank String defaultBranchName,
        @NotBlank String timezone,
        @NotBlank String taxZone,
        @NotBlank String currency
) {
}
