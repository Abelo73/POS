package com.novapos.customer.api;

import com.novapos.customer.api.dto.CustomerDto;
import com.novapos.customer.api.dto.LoyaltyLedgerDto;
import java.util.List; import java.util.Optional; import java.util.UUID;

public interface CustomerFacade {
    CustomerDto createCustomer(UUID companyId, String name, String email, String phone);
    Optional<CustomerDto> getCustomer(UUID customerId);
    CustomerDto updateCustomer(UUID customerId, String name, String email, String phone);
    void deleteCustomer(UUID customerId);
    List<CustomerDto> getCustomersByCompany(UUID companyId);
    void accruePoints(UUID customerId, int points, String reason, UUID referenceId);
    int redeemPoints(UUID customerId, int points, UUID referenceId);
    void issueStoreCredit(UUID customerId, long amountMinor, String reason, UUID referenceId);
    long spendStoreCredit(UUID customerId, long amountMinor, UUID referenceId);
    long getPointsBalance(UUID customerId);
    long getStoreCreditBalance(UUID customerId);
}
