package com.novapos.customer.domain;

import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name = "store_credit_ledger", schema = "customer")
public class StoreCreditLedger {
    @Id @Column(name = "id", nullable = false) private UUID id;
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "amount_delta_minor", nullable = false) private long amountDeltaMinor;
    @Column(name = "reason", nullable = false) private String reason;
    @Column(name = "reference_id") private UUID referenceId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    StoreCreditLedger() {}
    public StoreCreditLedger(UUID customerId, long amountDeltaMinor, String reason, UUID referenceId) {
        this.id = UUID.randomUUID(); this.customerId = customerId; this.amountDeltaMinor = amountDeltaMinor; this.reason = reason; this.referenceId = referenceId;
    }
    @PrePersist void prePersist() { if (this.id == null) this.id = UUID.randomUUID(); if (this.createdAt == null) this.createdAt = Instant.now(); }
    public UUID getId() { return id; } public UUID getCustomerId() { return customerId; } public long getAmountDeltaMinor() { return amountDeltaMinor; }
    public String getReason() { return reason; } public UUID getReferenceId() { return referenceId; } public Instant getCreatedAt() { return createdAt; }
}
