package com.novapos.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_role_assignment", schema = "user_access")
public class UserRoleAssignment {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    UserRoleAssignment() {
    }

    public UserRoleAssignment(UUID userId, UUID roleId, UUID branchId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.roleId = roleId;
        this.branchId = branchId;
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
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
