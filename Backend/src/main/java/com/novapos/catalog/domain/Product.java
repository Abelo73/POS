package com.novapos.catalog.domain;

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
@Table(name = "product", schema = "catalog")
public class Product {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "brand_id")
    private UUID brandId;

    @Column(name = "base_price_minor", nullable = false)
    private long basePriceMinor;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "tax_class", nullable = false)
    private String taxClass;

    @Column(name = "track_inventory", nullable = false)
    private boolean trackInventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "costing_method", nullable = false)
    private CostingMethod costingMethod;

    @Column(name = "is_composite", nullable = false)
    private boolean composite;

    @Column(name = "sold_by_weight", nullable = false)
    private boolean soldByWeight;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    Product() {
    }

    public Product(UUID companyId, String sku, String name, long basePriceMinor, String currency, String taxClass) {
        this.id = UUID.randomUUID();
        this.companyId = companyId;
        this.sku = sku;
        this.name = name;
        this.basePriceMinor = basePriceMinor;
        this.currency = currency;
        this.taxClass = taxClass;
        this.trackInventory = true;
        this.costingMethod = CostingMethod.FIFO;
        this.composite = false;
        this.soldByWeight = false;
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

    public UUID getCompanyId() {
        return companyId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public UUID getBrandId() {
        return brandId;
    }

    public void setBrandId(UUID brandId) {
        this.brandId = brandId;
    }

    public long getBasePriceMinor() {
        return basePriceMinor;
    }

    public void setBasePriceMinor(long basePriceMinor) {
        this.basePriceMinor = basePriceMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTaxClass() {
        return taxClass;
    }

    public void setTaxClass(String taxClass) {
        this.taxClass = taxClass;
    }

    public boolean isTrackInventory() {
        return trackInventory;
    }

    public void setTrackInventory(boolean trackInventory) {
        this.trackInventory = trackInventory;
    }

    public CostingMethod getCostingMethod() {
        return costingMethod;
    }

    public void setCostingMethod(CostingMethod costingMethod) {
        this.costingMethod = costingMethod;
    }

    public boolean isComposite() {
        return composite;
    }

    public void setComposite(boolean composite) {
        this.composite = composite;
    }

    public boolean isSoldByWeight() {
        return soldByWeight;
    }

    public void setSoldByWeight(boolean soldByWeight) {
        this.soldByWeight = soldByWeight;
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
