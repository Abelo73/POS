package com.novapos.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "stock_count_line", schema = "inventory")
public class StockCountLine {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "stock_count_id", nullable = false)
    private UUID stockCountId;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(name = "expected_quantity", nullable = false)
    private int expectedQuantity;

    @Column(name = "counted_quantity")
    private Integer countedQuantity;

    StockCountLine() {
    }

    public StockCountLine(UUID stockCountId, UUID productVariantId, int expectedQuantity) {
        this.id = UUID.randomUUID();
        this.stockCountId = stockCountId;
        this.productVariantId = productVariantId;
        this.expectedQuantity = expectedQuantity;
    }

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public UUID getStockCountId() { return stockCountId; }
    public UUID getProductVariantId() { return productVariantId; }
    public int getExpectedQuantity() { return expectedQuantity; }
    public Integer getCountedQuantity() { return countedQuantity; }
    public void setCountedQuantity(Integer countedQuantity) { this.countedQuantity = countedQuantity; }
}
