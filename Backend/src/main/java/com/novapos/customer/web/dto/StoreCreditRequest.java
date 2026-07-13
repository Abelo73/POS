package com.novapos.customer.web.dto;
import jakarta.validation.constraints.NotNull; import jakarta.validation.constraints.Positive; import java.util.UUID;

public record StoreCreditRequest(@NotNull UUID customerId, @Positive long amountMinor, String reason, String referenceId) {}
