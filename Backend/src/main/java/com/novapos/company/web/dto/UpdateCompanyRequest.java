package com.novapos.company.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateCompanyRequest(
        String name,
        @Size(min = 3, max = 3) String defaultCurrency
) {
}
