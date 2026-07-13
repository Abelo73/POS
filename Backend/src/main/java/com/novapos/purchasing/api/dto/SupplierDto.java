package com.novapos.purchasing.api.dto;

import java.util.UUID;

public record SupplierDto(UUID id, UUID companyId, String name, String paymentTerms, Integer leadTimeDays) {}
