package com.novapos.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_count", schema = "inventory")
public class StockCount {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "variance_threshold", nullable = false)
    private long varianceThreshold;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    StockCount() {
    }

    public StockCount(UUID locationId, long varianceThreshold) {
        this.id = UUID.randomUUID();
        this.locationId = locationId;
        this.status = "DRAFT";
        this.varianceThreshold = varianceThreshold;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getLocationId() { return locationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getVarianceThreshold() { return varianceThreshold; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void markUpdated() { this.updatedAt = Instant.now(); }
}
