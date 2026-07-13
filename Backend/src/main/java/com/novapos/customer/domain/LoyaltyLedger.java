package com.novapos.customer.domain;

import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name = "loyalty_ledger", schema = "customer")
public class LoyaltyLedger {
    @Id @Column(name = "id", nullable = false) private UUID id;
    @Column(name = "customer_id", nullable = false) private UUID customerId;
    @Column(name = "points_delta", nullable = false) private int pointsDelta;
    @Column(name = "reason", nullable = false) private String reason;
    @Column(name = "reference_id") private UUID referenceId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    LoyaltyLedger() {}
    public LoyaltyLedger(UUID customerId, int pointsDelta, String reason, UUID referenceId) {
        this.id = UUID.randomUUID(); this.customerId = customerId; this.pointsDelta = pointsDelta; this.reason = reason; this.referenceId = referenceId;
    }
    @PrePersist void prePersist() { if (this.id == null) this.id = UUID.randomUUID(); if (this.createdAt == null) this.createdAt = Instant.now(); }
    public UUID getId() { return id; } public UUID getCustomerId() { return customerId; } public int getPointsDelta() { return pointsDelta; }
    public String getReason() { return reason; } public UUID getReferenceId() { return referenceId; } public Instant getCreatedAt() { return createdAt; }
}
