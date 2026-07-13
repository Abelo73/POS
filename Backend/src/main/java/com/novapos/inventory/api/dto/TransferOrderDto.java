package com.novapos.inventory.api.dto;

import java.util.List;
import java.util.UUID;

public record TransferOrderDto(
        UUID id,
        UUID sourceLocationId,
        UUID destinationLocationId,
        String status,
        List<TransferLineDto> lines
) {
}
