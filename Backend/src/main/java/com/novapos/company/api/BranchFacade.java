package com.novapos.company.api;

import com.novapos.company.api.dto.BranchDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface BranchFacade {

    BranchDto createBranch(UUID companyId, String name, String timezone, String taxZone, String currency, Map<String, Object> address, Map<String, Object> openingHours);

    Optional<BranchDto> getBranch(UUID branchId, UUID companyId);

    List<BranchDto> getBranchesByCompany(UUID companyId);

    BranchDto updateBranch(UUID branchId, UUID companyId, String name, String timezone, String taxZone, String currency);

    void deleteBranch(UUID branchId, UUID companyId);
}
