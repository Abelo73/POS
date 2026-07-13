package com.novapos.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bundle_component", schema = "catalog")
public class BundleComponent {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "bundle_product_id", nullable = false)
    private UUID bundleProductId;

    @Column(name = "component_product_id", nullable = false)
    private UUID componentProductId;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    BundleComponent() {
    }

    public BundleComponent(UUID bundleProductId, UUID componentProductId, BigDecimal quantity) {
        this.id = UUID.randomUUID();
        this.bundleProductId = bundleProductId;
        this.componentProductId = componentProductId;
        this.quantity = quantity;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getBundleProductId() {
        return bundleProductId;
    }

    public UUID getComponentProductId() {
        return componentProductId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markUpdated() {
        this.updatedAt = Instant.now();
    }
}
