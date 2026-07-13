package com.novapos.purchasing.repository;

import com.novapos.purchasing.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Optional<Supplier> findByIdAndDeletedAtIsNull(UUID id);
    List<Supplier> findByCompanyIdAndDeletedAtIsNull(UUID companyId);
}
