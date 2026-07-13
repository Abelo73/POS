package com.novapos.catalog.repository;

import com.novapos.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndDeletedAtIsNull(UUID id);

    List<Product> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

    List<Product> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);

    List<Product> findByBrandIdAndDeletedAtIsNull(UUID brandId);

    boolean existsBySkuAndCompanyIdAndDeletedAtIsNull(String sku, UUID companyId);

    Optional<Product> findBySkuAndCompanyIdAndDeletedAtIsNull(String sku, UUID companyId);
}
