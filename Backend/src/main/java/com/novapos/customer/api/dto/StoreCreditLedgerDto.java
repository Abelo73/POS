package com.novapos.customer.api.dto;
import java.util.UUID;
public record StoreCreditLedgerDto(UUID id, UUID customerId, long amountDeltaMinor, String reason, UUID referenceId) {}
