package com.novapos.pos.domain;

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
@Table(name = "payment", schema = "pos")
public class Payment {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "sale_id", nullable = false)
    private UUID saleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "reference")
    private String reference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    Payment() {
    }

    public Payment(UUID saleId, PaymentMethod method, long amountMinor, String reference) {
        this.id = UUID.randomUUID();
        this.saleId = saleId;
        this.method = method;
        this.amountMinor = amountMinor;
        this.reference = reference;
    }

    @PrePersist
    void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getSaleId() { return saleId; }
    public PaymentMethod getMethod() { return method; }
    public long getAmountMinor() { return amountMinor; }
    public String getReference() { return reference; }
    public Instant getCreatedAt() { return createdAt; }
}
