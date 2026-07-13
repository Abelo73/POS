package com.novapos.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_variant", schema = "catalog")
public class ProductVariant {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "price_override_minor")
    private Long priceOverrideMinor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    ProductVariant() {
    }

    public ProductVariant(UUID productId, String variantName, String barcode, Long priceOverrideMinor) {
        this.id = UUID.randomUUID();
        this.productId = productId;
        this.variantName = variantName;
        this.barcode = barcode;
        this.priceOverrideMinor = priceOverrideMinor;
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

    public UUID getProductId() {
        return productId;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getPriceOverrideMinor() {
        return priceOverrideMinor;
    }

    public void setPriceOverrideMinor(Long priceOverrideMinor) {
        this.priceOverrideMinor = priceOverrideMinor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void markUpdated() {
        this.updatedAt = Instant.now();
    }
}
