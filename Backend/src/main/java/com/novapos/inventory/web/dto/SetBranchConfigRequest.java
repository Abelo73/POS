package com.novapos.inventory.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SetBranchConfigRequest(
        @NotNull UUID branchId,
        boolean allowNegativeStock
) {
}
