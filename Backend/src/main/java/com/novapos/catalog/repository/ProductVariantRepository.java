package com.novapos.catalog.repository;

import com.novapos.catalog.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    Optional<ProductVariant> findByIdAndDeletedAtIsNull(UUID id);

    List<ProductVariant> findByProductIdAndDeletedAtIsNull(UUID productId);
}
