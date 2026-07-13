package com.novapos.catalog.repository;

import com.novapos.catalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByIdAndDeletedAtIsNull(UUID id);

    List<Category> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

    List<Category> findByParentIdAndDeletedAtIsNull(UUID parentId);

    boolean existsByNameAndCompanyIdAndDeletedAtIsNull(String name, UUID companyId);
}
