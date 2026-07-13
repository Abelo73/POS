package com.novapos.purchasing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "supplier", schema = "purchasing")
public class Supplier {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "payment_terms")
    private String paymentTerms;
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "deleted_at")
    private Instant deletedAt;

    Supplier() {}
    public Supplier(UUID companyId, String name, String paymentTerms, Integer leadTimeDays) {
        this.id = UUID.randomUUID(); this.companyId = companyId; this.name = name;
        this.paymentTerms = paymentTerms; this.leadTimeDays = leadTimeDays;
    }
    @PrePersist void prePersist() {
        var now = Instant.now();
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public UUID getCompanyId() { return companyId; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String t) { this.paymentTerms = t; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer d) { this.leadTimeDays = d; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant d) { this.deletedAt = d; }
    public void markUpdated() { this.updatedAt = Instant.now(); }
}
