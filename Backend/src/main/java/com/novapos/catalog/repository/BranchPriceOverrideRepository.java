package com.novapos.catalog.repository;

import com.novapos.catalog.domain.BranchPriceOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchPriceOverrideRepository extends JpaRepository<BranchPriceOverride, UUID> {

    Optional<BranchPriceOverride> findByProductIdAndBranchId(UUID productId, UUID branchId);
}
