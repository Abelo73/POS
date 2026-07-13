package com.novapos.customer.web.dto;
import jakarta.validation.constraints.NotNull; import jakarta.validation.constraints.Positive; import java.util.UUID;
public record LoyaltyRequest(@NotNull UUID customerId, @Positive int points, String referenceId) {}
