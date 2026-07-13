package com.novapos.inventory.repository;

import com.novapos.inventory.domain.ReorderConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReorderConfigRepository extends JpaRepository<ReorderConfig, UUID> {
    Optional<ReorderConfig> findByProductVariantIdAndLocationId(UUID productVariantId, UUID locationId);
}
