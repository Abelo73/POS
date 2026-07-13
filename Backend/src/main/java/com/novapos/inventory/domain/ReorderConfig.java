package com.novapos.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reorder_config", schema = "inventory")
public class ReorderConfig {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "reorder_level", nullable = false)
    private int reorderLevel;

    @Column(name = "reorder_quantity", nullable = false)
    private int reorderQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    ReorderConfig() {
    }

    public ReorderConfig(UUID productVariantId, UUID locationId, int reorderLevel, int reorderQuantity) {
        this.id = UUID.randomUUID();
        this.productVariantId = productVariantId;
        this.locationId = locationId;
        this.reorderLevel = reorderLevel;
        this.reorderQuantity = reorderQuantity;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getProductVariantId() { return productVariantId; }
    public UUID getLocationId() { return locationId; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public int getReorderQuantity() { return reorderQuantity; }
    public void setReorderQuantity(int reorderQuantity) { this.reorderQuantity = reorderQuantity; }
    public void markUpdated() { this.updatedAt = Instant.now(); }
}
