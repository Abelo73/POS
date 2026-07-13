package com.novapos.pos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sale_line", schema = "pos")
public class SaleLine {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "sale_id", nullable = false)
    private UUID saleId;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price_minor", nullable = false)
    private long unitPriceMinor;

    @Column(name = "discount_minor", nullable = false)
    private long discountMinor;

    @Column(name = "tax_minor", nullable = false)
    private long taxMinor;

    SaleLine() {
    }

    public SaleLine(UUID saleId, UUID productVariantId, BigDecimal quantity, long unitPriceMinor) {
        this.id = UUID.randomUUID();
        this.saleId = saleId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
        this.unitPriceMinor = unitPriceMinor;
    }

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public UUID getSaleId() { return saleId; }
    public UUID getProductVariantId() { return productVariantId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public long getUnitPriceMinor() { return unitPriceMinor; }
    public void setUnitPriceMinor(long unitPriceMinor) { this.unitPriceMinor = unitPriceMinor; }
    public long getDiscountMinor() { return discountMinor; }
    public void setDiscountMinor(long discountMinor) { this.discountMinor = discountMinor; }
    public long getTaxMinor() { return taxMinor; }
    public void setTaxMinor(long taxMinor) { this.taxMinor = taxMinor; }
}
