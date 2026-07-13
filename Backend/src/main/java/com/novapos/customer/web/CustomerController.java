package com.novapos.customer.web;

import com.novapos.customer.api.CustomerFacade;
import com.novapos.customer.api.dto.CustomerDto;
import com.novapos.customer.web.dto.CreateCustomerRequest;
import com.novapos.customer.web.dto.LoyaltyRequest;
import com.novapos.customer.web.dto.StoreCreditRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List; import java.util.Map; import java.util.UUID;

@RestController @RequestMapping("/api/v1")
class CustomerController {
    private final CustomerFacade cf;
    CustomerController(CustomerFacade cf) { this.cf = cf; }

    @PostMapping("/companies/{companyId}/customers")
    ResponseEntity<CustomerDto> create(@PathVariable UUID companyId, @Valid @RequestBody CreateCustomerRequest r) {
        var c = cf.createCustomer(companyId, r.name(), r.email(), r.phone());
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(c.id()).toUri();
        return ResponseEntity.created(uri).body(c);
    }
    @GetMapping("/customers/{id}") ResponseEntity<CustomerDto> get(@PathVariable UUID id) { return cf.getCustomer(id).map(ResponseEntity::ok).orElseThrow(() -> CustomerException.notFound(id)); }
    @PutMapping("/customers/{id}") ResponseEntity<CustomerDto> update(@PathVariable UUID id, @Valid @RequestBody CreateCustomerRequest r) { return ResponseEntity.ok(cf.updateCustomer(id, r.name(), r.email(), r.phone())); }
    @DeleteMapping("/customers/{id}") ResponseEntity<Void> delete(@PathVariable UUID id) { cf.deleteCustomer(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/companies/{companyId}/customers") ResponseEntity<List<CustomerDto>> list(@PathVariable UUID companyId) { return ResponseEntity.ok(cf.getCustomersByCompany(companyId)); }

    @PostMapping("/customers/loyalty/accrue") ResponseEntity<Void> accruePoints(@Valid @RequestBody LoyaltyRequest r) { cf.accruePoints(r.customerId(), r.points(), "ACCRUAL", r.referenceId() != null ? UUID.fromString(r.referenceId()) : null); return ResponseEntity.ok().build(); }
    @PostMapping("/customers/loyalty/redeem") ResponseEntity<Map<String,Integer>> redeem(@Valid @RequestBody LoyaltyRequest r) { var bal = cf.redeemPoints(r.customerId(), r.points(), r.referenceId() != null ? UUID.fromString(r.referenceId()) : null); return ResponseEntity.ok(Map.of("newBalance", bal)); }
    @GetMapping("/customers/{id}/loyalty-balance") ResponseEntity<Map<String,Long>> pointsBalance(@PathVariable UUID id) { return ResponseEntity.ok(Map.of("points", cf.getPointsBalance(id))); }

    @PostMapping("/customers/store-credit/issue") ResponseEntity<Void> issueCredit(@Valid @RequestBody StoreCreditRequest r) { cf.issueStoreCredit(r.customerId(), r.amountMinor(), r.reason(), r.referenceId() != null ? UUID.fromString(r.referenceId()) : null); return ResponseEntity.ok().build(); }
    @PostMapping("/customers/store-credit/spend") ResponseEntity<Map<String,Long>> spendCredit(@Valid @RequestBody StoreCreditRequest r) { var bal = cf.spendStoreCredit(r.customerId(), r.amountMinor(), r.referenceId() != null ? UUID.fromString(r.referenceId()) : null); return ResponseEntity.ok(Map.of("newBalance", bal)); }
    @GetMapping("/customers/{id}/credit-balance") ResponseEntity<Map<String,Long>> creditBalance(@PathVariable UUID id) { return ResponseEntity.ok(Map.of("balance", cf.getStoreCreditBalance(id))); }
}
