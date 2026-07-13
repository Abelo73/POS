package com.novapos.pos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sale", schema = "pos")
public class Sale {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "cashier_id", nullable = false)
    private UUID cashierId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SaleStatus status;

    @Column(name = "subtotal_minor", nullable = false)
    private long subtotalMinor;

    @Column(name = "discount_minor", nullable = false)
    private long discountMinor;

    @Column(name = "tax_minor", nullable = false)
    private long taxMinor;

    @Column(name = "total_minor", nullable = false)
    private long totalMinor;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "client_uuid", nullable = false, unique = true)
    private UUID clientUuid;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    Sale() {
    }

    public Sale(UUID branchId, UUID cashierId, UUID customerId, String currency, UUID clientUuid) {
        this.id = UUID.randomUUID();
        this.branchId = branchId;
        this.cashierId = cashierId;
        this.customerId = customerId;
        this.currency = currency;
        this.clientUuid = clientUuid;
        this.status = SaleStatus.OPEN;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getBranchId() { return branchId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getCashierId() { return cashierId; }
    public SaleStatus getStatus() { return status; }
    public long getSubtotalMinor() { return subtotalMinor; }
    public long getDiscountMinor() { return discountMinor; }
    public long getTaxMinor() { return taxMinor; }
    public long getTotalMinor() { return totalMinor; }
    public String getCurrency() { return currency; }
    public UUID getClientUuid() { return clientUuid; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(SaleStatus status) { this.status = status; }
    public void setSubtotalMinor(long subtotalMinor) { this.subtotalMinor = subtotalMinor; }
    public void setDiscountMinor(long discountMinor) { this.discountMinor = discountMinor; }
    public void setTaxMinor(long taxMinor) { this.taxMinor = taxMinor; }
    public void setTotalMinor(long totalMinor) { this.totalMinor = totalMinor; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public void markUpdated() { this.updatedAt = Instant.now(); }

    public void recalculateTotals(long subtotal, long discount, long tax) {
        this.subtotalMinor = subtotal;
        this.discountMinor = discount;
        this.taxMinor = tax;
        this.totalMinor = subtotal + tax - discount;
    }
}
