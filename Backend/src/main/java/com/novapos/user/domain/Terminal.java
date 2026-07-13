package com.novapos.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "terminal", schema = "user_access")
public class Terminal {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    Terminal() {
    }

    public Terminal(UUID branchId, String deviceFingerprint) {
        this.id = UUID.randomUUID();
        this.branchId = branchId;
        this.deviceFingerprint = deviceFingerprint;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.registeredAt == null) {
            this.registeredAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }
}
