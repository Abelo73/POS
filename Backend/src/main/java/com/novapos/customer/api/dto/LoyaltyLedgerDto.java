package com.novapos.customer.api.dto;
import java.util.UUID;
public record LoyaltyLedgerDto(UUID id, UUID customerId, int pointsDelta, String reason, UUID referenceId) {}
