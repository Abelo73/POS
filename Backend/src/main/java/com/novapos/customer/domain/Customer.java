package com.novapos.customer.domain;

import jakarta.persistence.Column; import jakarta.persistence.Entity; import jakarta.persistence.Id;
import jakarta.persistence.PrePersist; import jakarta.persistence.Table;
import java.time.Instant; import java.util.UUID;

@Entity @Table(name = "customer", schema = "customer")
public class Customer {
    @Id @Column(name = "id", nullable = false) private UUID id;
    @Column(name = "company_id", nullable = false) private UUID companyId;
    @Column(name = "name", nullable = false) private String name;
    @Column(name = "email") private String email;
    @Column(name = "phone") private String phone;
    @Column(name = "credit_limit_minor", nullable = false) private long creditLimitMinor;
    @Column(name = "loyalty_tier") private String loyaltyTier;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "deleted_at") private Instant deletedAt;
    Customer() {}
    public Customer(UUID companyId, String name, String email, String phone) {
        this.id = UUID.randomUUID(); this.companyId = companyId; this.name = name; this.email = email; this.phone = phone;
    }
    @PrePersist void prePersist() { var now = Instant.now(); if (this.id == null) this.id = UUID.randomUUID(); if (this.createdAt == null) this.createdAt = now; if (this.updatedAt == null) this.updatedAt = now; }
    public UUID getId() { return id; } public UUID getCompanyId() { return companyId; } public String getName() { return name; }
    public void setName(String n) { this.name = n; } public String getEmail() { return email; } public void setEmail(String e) { this.email = e; }
    public String getPhone() { return phone; } public void setPhone(String p) { this.phone = p; }
    public long getCreditLimitMinor() { return creditLimitMinor; } public void setCreditLimitMinor(long l) { this.creditLimitMinor = l; }
    public String getLoyaltyTier() { return loyaltyTier; } public void setLoyaltyTier(String t) { this.loyaltyTier = t; }
    public Instant getDeletedAt() { return deletedAt; } public void setDeletedAt(Instant d) { this.deletedAt = d; }
    public Instant getCreatedAt() { return createdAt; }
    public void markUpdated() { this.updatedAt = Instant.now(); }
}
