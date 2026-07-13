package com.novapos.purchasing.web.dto;

public record UpdateSupplierRequest(String name, String paymentTerms, Integer leadTimeDays) {}
