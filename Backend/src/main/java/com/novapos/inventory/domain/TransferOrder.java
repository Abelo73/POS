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
@Table(name = "transfer_order", schema = "inventory")
public class TransferOrder {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "source_location_id", nullable = false)
    private UUID sourceLocationId;

    @Column(name = "destination_location_id", nullable = false)
    private UUID destinationLocationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    TransferOrder() {
    }

    public TransferOrder(UUID sourceLocationId, UUID destinationLocationId) {
        this.id = UUID.randomUUID();
        this.sourceLocationId = sourceLocationId;
        this.destinationLocationId = destinationLocationId;
        this.status = TransferStatus.REQUESTED;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getSourceLocationId() { return sourceLocationId; }
    public UUID getDestinationLocationId() { return destinationLocationId; }
    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void markUpdated() { this.updatedAt = Instant.now(); }
}
