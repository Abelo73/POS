package com.novapos.purchasing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "purchase_order", schema = "purchasing")
public class PurchaseOrder {

    @Id @Column(name = "id", nullable = false) private UUID id;
    @Column(name = "supplier_id", nullable = false) private UUID supplierId;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) private PurchaseOrderStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    PurchaseOrder() {}
    public PurchaseOrder(UUID supplierId, UUID branchId) {
        this.id = UUID.randomUUID(); this.supplierId = supplierId; this.branchId = branchId;
        this.status = PurchaseOrderStatus.DRAFT;
    }
    @PrePersist void prePersist() {
        var now = Instant.now();
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getSupplierId() { return supplierId; }
    public UUID getBranchId() { return branchId; }
    public PurchaseOrderStatus getStatus() { return status; }
    public void setStatus(PurchaseOrderStatus s) { this.status = s; }
    public Instant getCreatedAt() { return createdAt; }
    public void markUpdated() { this.updatedAt = Instant.now(); }
}
