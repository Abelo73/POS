package com.novapos.purchasing.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record POLineDto(UUID id, UUID purchaseOrderId, UUID productVariantId, BigDecimal quantityOrdered, BigDecimal quantityReceived, long unitCostMinor) {}
