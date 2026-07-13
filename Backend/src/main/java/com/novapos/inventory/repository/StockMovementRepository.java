package com.novapos.inventory.repository;

import com.novapos.inventory.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByProductVariantIdAndLocationIdOrderByCreatedAtAsc(
            UUID productVariantId, UUID locationId);

    @Query("select coalesce(sum(sm.quantityDelta), 0) from StockMovement sm "
            + "where sm.productVariantId = :variantId and sm.locationId = :locationId")
    int sumQuantityDelta(@Param("variantId") UUID productVariantId, @Param("locationId") UUID locationId);
}
