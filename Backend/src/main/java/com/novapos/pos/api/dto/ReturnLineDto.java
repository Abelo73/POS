package com.novapos.pos.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReturnLineDto(
        UUID id,
        UUID originalSaleLineId,
        UUID returnSaleId,
        BigDecimal quantity,
        String refundMethod
) {
}
