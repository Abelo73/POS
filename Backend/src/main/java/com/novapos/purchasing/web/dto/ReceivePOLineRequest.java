package com.novapos.purchasing.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReceivePOLineRequest(@NotNull @Positive BigDecimal quantityReceived) {}
