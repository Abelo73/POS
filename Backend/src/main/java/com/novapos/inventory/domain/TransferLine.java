package com.novapos.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfer_line", schema = "inventory")
public class TransferLine {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "transfer_order_id", nullable = false)
    private UUID transferOrderId;

    @Column(name = "product_variant_id", nullable = false)
    private UUID productVariantId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    TransferLine() {
    }

    public TransferLine(UUID transferOrderId, UUID productVariantId, int quantity) {
        this.id = UUID.randomUUID();
        this.transferOrderId = transferOrderId;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = now;
    }

    public UUID getId() { return id; }
    public UUID getTransferOrderId() { return transferOrderId; }
    public UUID getProductVariantId() { return productVariantId; }
    public int getQuantity() { return quantity; }
}
