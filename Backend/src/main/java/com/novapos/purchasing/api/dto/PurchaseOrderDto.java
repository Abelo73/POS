package com.novapos.purchasing.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderDto(UUID id, UUID supplierId, UUID branchId, String status, List<POLineDto> lines) {}
