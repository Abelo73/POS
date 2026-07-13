package com.novapos.purchasing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "purchase_order_line", schema = "purchasing")
public class PurchaseOrderLine {

    @Id @Column(name = "id", nullable = false) private UUID id;
    @Column(name = "purchase_order_id", nullable = false) private UUID purchaseOrderId;
    @Column(name = "product_variant_id", nullable = false) private UUID productVariantId;
    @Column(name = "quantity_ordered", nullable = false) private BigDecimal quantityOrdered;
    @Column(name = "quantity_received", nullable = false) private BigDecimal quantityReceived;
    @Column(name = "unit_cost_minor", nullable = false) private long unitCostMinor;

    PurchaseOrderLine() {}
    public PurchaseOrderLine(UUID purchaseOrderId, UUID productVariantId, BigDecimal quantityOrdered, long unitCostMinor) {
        this.id = UUID.randomUUID(); this.purchaseOrderId = purchaseOrderId;
        this.productVariantId = productVariantId; this.quantityOrdered = quantityOrdered;
        this.quantityReceived = BigDecimal.ZERO; this.unitCostMinor = unitCostMinor;
    }
    @PrePersist void prePersist() { if (this.id == null) this.id = UUID.randomUUID(); }

    public UUID getId() { return id; }
    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public UUID getProductVariantId() { return productVariantId; }
    public BigDecimal getQuantityOrdered() { return quantityOrdered; }
    public BigDecimal getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(BigDecimal q) { this.quantityReceived = q; }
    public long getUnitCostMinor() { return unitCostMinor; }
}
