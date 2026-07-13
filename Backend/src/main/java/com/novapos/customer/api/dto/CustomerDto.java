package com.novapos.customer.api.dto;
import java.time.Instant; import java.util.UUID;
public record CustomerDto(UUID id, UUID companyId, String name, String email, String phone, long creditLimitMinor, String loyaltyTier, int loyaltyPoints, long storeCreditBalance, Instant createdAt) {}
