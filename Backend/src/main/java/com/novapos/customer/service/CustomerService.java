package com.novapos.customer.service;

import com.novapos.customer.api.CustomerFacade;
import com.novapos.customer.api.dto.CustomerDto;
import com.novapos.customer.domain.*;
import com.novapos.customer.repository.*;
import com.novapos.customer.web.CustomerException;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.Instant; import java.util.*; import java.util.stream.Collectors;

@Service @Transactional
class CustomerService implements CustomerFacade {
    private final CustomerRepository cr; private final LoyaltyLedgerRepository llr; private final StoreCreditLedgerRepository sclr;
    CustomerService(CustomerRepository cr, LoyaltyLedgerRepository llr, StoreCreditLedgerRepository sclr) { this.cr = cr; this.llr = llr; this.sclr = sclr; }

    @Override public CustomerDto createCustomer(UUID companyId, String name, String email, String phone) { return toDto(cr.save(new Customer(companyId, name, email, phone))); }
    @Override public Optional<CustomerDto> getCustomer(UUID id) { return cr.findByIdAndDeletedAtIsNull(id).map(this::toDto); }
    @Override public CustomerDto updateCustomer(UUID id, String name, String email, String phone) {
        var c = cr.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> CustomerException.notFound(id));
        if (name != null) c.setName(name); if (email != null) c.setEmail(email); if (phone != null) c.setPhone(phone);
        c.markUpdated(); return toDto(cr.save(c));
    }
    @Override public void deleteCustomer(UUID id) { var c = cr.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> CustomerException.notFound(id)); c.setDeletedAt(Instant.now()); cr.save(c); }
    @Override public List<CustomerDto> getCustomersByCompany(UUID companyId) { return cr.findByCompanyIdAndDeletedAtIsNull(companyId).stream().map(this::toDto).toList(); }
    @Override public void accruePoints(UUID customerId, int points, String reason, UUID referenceId) {
        cr.findByIdAndDeletedAtIsNull(customerId).orElseThrow(() -> CustomerException.notFound(customerId));
        llr.save(new LoyaltyLedger(customerId, points, reason, referenceId));
    }
    @Override public int redeemPoints(UUID customerId, int points, UUID referenceId) {
        cr.findByIdAndDeletedAtIsNull(customerId).orElseThrow(() -> CustomerException.notFound(customerId));
        var balance = llr.sumPointsDelta(customerId);
        if (balance < points) throw CustomerException.insufficientPoints(customerId, points, balance);
        llr.save(new LoyaltyLedger(customerId, -points, "REDEMPTION", referenceId));
        return balance - points;
    }
    @Override public void issueStoreCredit(UUID customerId, long amountMinor, String reason, UUID referenceId) {
        cr.findByIdAndDeletedAtIsNull(customerId).orElseThrow(() -> CustomerException.notFound(customerId));
        sclr.save(new StoreCreditLedger(customerId, amountMinor, reason, referenceId));
    }
    @Override public long spendStoreCredit(UUID customerId, long amountMinor, UUID referenceId) {
        cr.findByIdAndDeletedAtIsNull(customerId).orElseThrow(() -> CustomerException.notFound(customerId));
        var balance = sclr.sumAmountDelta(customerId);
        if (balance < amountMinor) throw CustomerException.insufficientCredit(customerId, amountMinor, balance);
        sclr.save(new StoreCreditLedger(customerId, -amountMinor, "SPEND", referenceId));
        return balance - amountMinor;
    }
    @Override public long getPointsBalance(UUID customerId) { return llr.sumPointsDelta(customerId); }
    @Override public long getStoreCreditBalance(UUID customerId) { return sclr.sumAmountDelta(customerId); }

    CustomerDto toDto(Customer c) {
        return new CustomerDto(c.getId(), c.getCompanyId(), c.getName(), c.getEmail(), c.getPhone(), c.getCreditLimitMinor(), c.getLoyaltyTier(), llr.sumPointsDelta(c.getId()), sclr.sumAmountDelta(c.getId()), c.getCreatedAt());
    }
}
