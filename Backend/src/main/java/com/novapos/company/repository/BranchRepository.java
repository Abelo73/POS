package com.novapos.company.repository;

import com.novapos.company.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByIdAndDeletedAtIsNull(UUID id);

    List<Branch> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

    Optional<Branch> findByIdAndCompanyIdAndDeletedAtIsNull(UUID id, UUID companyId);

    long countByCompanyIdAndDeletedAtIsNull(UUID companyId);
}
