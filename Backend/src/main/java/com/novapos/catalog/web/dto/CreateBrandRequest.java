package com.novapos.catalog.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBrandRequest(
        @NotBlank String name
) {
}
