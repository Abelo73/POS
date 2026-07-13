package com.novapos.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "batch", schema = "inventory")
public class Batch {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(name = "batch_code")
    private String batchCode;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    Batch() {
    }

    public Batch(UUID productVariantId, String batchCode, LocalDate expiryDate) {
        this.id = UUID.randomUUID();
        this.productVariantId = productVariantId;
        this.batchCode = batchCode;
        this.expiryDate = expiryDate;
        this.receivedAt = Instant.now();
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
        if (this.receivedAt == null) {
            this.receivedAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductVariantId() {
        return productVariantId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
