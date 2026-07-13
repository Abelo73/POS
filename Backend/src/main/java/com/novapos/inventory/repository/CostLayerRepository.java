package com.novapos.inventory.repository;

import com.novapos.inventory.domain.CostLayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CostLayerRepository extends JpaRepository<CostLayer, UUID> {

    List<CostLayer> findByProductVariantIdAndLocationIdAndQuantityGreaterThanOrderByCreatedAtAsc(
            UUID productVariantId, UUID locationId, int minQuantity);

    List<CostLayer> findByProductVariantIdAndLocationIdAndQuantityGreaterThanOrderByCreatedAtDesc(
            UUID productVariantId, UUID locationId, int minQuantity);
}
