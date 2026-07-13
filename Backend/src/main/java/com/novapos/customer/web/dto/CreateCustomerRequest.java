package com.novapos.customer.web.dto;
import jakarta.validation.constraints.NotBlank;
public record CreateCustomerRequest(@NotBlank String name, String email, String phone) {}
