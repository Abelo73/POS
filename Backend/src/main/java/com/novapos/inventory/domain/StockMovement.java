package com.novapos.inventory.domain;

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
@Table(name = "stock_movement", schema = "inventory")
public class StockMovement {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "quantity_delta", nullable = false)
    private int quantityDelta;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private MovementReason reason;

    @Column(name = "unit_cost_minor")
    private Long unitCostMinor;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    StockMovement() {
    }

    public StockMovement(UUID productVariantId, UUID locationId, int quantityDelta,
                         MovementReason reason, Long unitCostMinor, UUID batchId,
                         String referenceType, UUID referenceId, UUID createdBy) {
        this.id = UUID.randomUUID();
        this.productVariantId = productVariantId;
        this.locationId = locationId;
        this.quantityDelta = quantityDelta;
        this.reason = reason;
        this.unitCostMinor = unitCostMinor;
        this.batchId = batchId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.createdBy = createdBy;
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

    public int getQuantityDelta() {
        return quantityDelta;
    }

    public MovementReason getReason() {
        return reason;
    }

    public Long getUnitCostMinor() {
        return unitCostMinor;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }
}
