package com.novapos.inventory.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TransferStatusRequest(
        @NotBlank String status
) {
}
