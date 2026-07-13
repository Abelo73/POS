package com.novapos.catalog.repository;

import com.novapos.catalog.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findByIdAndDeletedAtIsNull(UUID id);

    List<Brand> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

    boolean existsByNameAndCompanyIdAndDeletedAtIsNull(String name, UUID companyId);
}
