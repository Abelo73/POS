package com.novapos.pos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "return_line", schema = "pos")
public class ReturnLine {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "original_sale_line_id", nullable = false)
    private UUID originalSaleLineId;

    @Column(name = "return_sale_id", nullable = false)
    private UUID returnSaleId;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "refund_method", nullable = false)
    private String refundMethod;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    ReturnLine() {
    }

    public ReturnLine(UUID originalSaleLineId, UUID returnSaleId, BigDecimal quantity, String refundMethod) {
        this.id = UUID.randomUUID();
        this.originalSaleLineId = originalSaleLineId;
        this.returnSaleId = returnSaleId;
        this.quantity = quantity;
        this.refundMethod = refundMethod;
    }

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOriginalSaleLineId() { return originalSaleLineId; }
    public UUID getReturnSaleId() { return returnSaleId; }
    public BigDecimal getQuantity() { return quantity; }
    public String getRefundMethod() { return refundMethod; }
    public Instant getCreatedAt() { return createdAt; }
}
