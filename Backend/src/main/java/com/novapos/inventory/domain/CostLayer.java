package com.novapos.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cost_layer", schema = "inventory")
public class CostLayer {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_cost_minor", nullable = false)
    private long unitCostMinor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    CostLayer() {
    }

    public CostLayer(UUID productVariantId, UUID locationId, int quantity, long unitCostMinor) {
        this.id = UUID.randomUUID();
        this.productVariantId = productVariantId;
        this.locationId = locationId;
        this.quantity = quantity;
        this.unitCostMinor = unitCostMinor;
    }

    @PrePersist
    void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductVariantId() {
        return productVariantId;
    }

    public UUID getLocationId() {
        return locationId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getUnitCostMinor() {
        return unitCostMinor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
