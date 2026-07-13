package com.novapos.inventory.repository;

import com.novapos.inventory.domain.BranchInventoryConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchInventoryConfigRepository extends JpaRepository<BranchInventoryConfig, UUID> {
    Optional<BranchInventoryConfig> findByBranchId(UUID branchId);
}
