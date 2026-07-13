package com.novapos.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "branch_inventory_config", schema = "inventory")
public class BranchInventoryConfig {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "allow_negative_stock", nullable = false)
    private boolean allowNegativeStock;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    BranchInventoryConfig() {
    }

    public BranchInventoryConfig(UUID branchId, boolean allowNegativeStock) {
        this.id = UUID.randomUUID();
        this.branchId = branchId;
        this.allowNegativeStock = allowNegativeStock;
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
    public boolean isAllowNegativeStock() { return allowNegativeStock; }
    public void setAllowNegativeStock(boolean allowNegativeStock) { this.allowNegativeStock = allowNegativeStock; }
    public void markUpdated() { this.updatedAt = Instant.now(); }
}
