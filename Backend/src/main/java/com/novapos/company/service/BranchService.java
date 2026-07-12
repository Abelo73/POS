package com.novapos.company.service;

import com.novapos.company.api.BranchFacade;
import com.novapos.company.api.dto.BranchDto;
import com.novapos.company.domain.Branch;
import com.novapos.company.repository.BranchRepository;
import com.novapos.company.web.CompanyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
class BranchService implements BranchFacade {

    private final BranchRepository branchRepository;

    BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    public BranchDto createBranch(UUID companyId, String name, String timezone, String taxZone, String currency, Map<String, Object> address, Map<String, Object> openingHours) {
        var branch = new Branch(companyId, name, timezone, taxZone, currency);
        if (address != null) {
            branch.setAddress(address);
        }
        if (openingHours != null) {
            branch.setOpeningHours(openingHours);
        }
        branch = branchRepository.save(branch);
        return toDto(branch);
    }

    @Override
    public Optional<BranchDto> getBranch(UUID branchId, UUID companyId) {
        return branchRepository.findByIdAndCompanyIdAndDeletedAtIsNull(branchId, companyId)
                .map(BranchService::toDto);
    }

    @Override
    public List<BranchDto> getBranchesByCompany(UUID companyId) {
        return branchRepository.findByCompanyIdAndDeletedAtIsNull(companyId).stream()
                .map(BranchService::toDto)
                .toList();
    }

    @Override
    public BranchDto updateBranch(UUID branchId, UUID companyId, String name, String timezone, String taxZone, String currency) {
        var branch = branchRepository.findByIdAndCompanyIdAndDeletedAtIsNull(branchId, companyId)
                .orElseThrow(() -> CompanyException.branchNotFound(branchId));
        if (name != null) {
            branch.setName(name);
        }
        if (timezone != null) {
            branch.setTimezone(timezone);
        }
        if (taxZone != null) {
            branch.setTaxZone(taxZone);
        }
        if (currency != null) {
            branch.setCurrency(currency);
        }
        branch.markUpdated();
        branch = branchRepository.save(branch);
        return toDto(branch);
    }

    @Override
    public void deleteBranch(UUID branchId, UUID companyId) {
        long activeCount = branchRepository.countByCompanyIdAndDeletedAtIsNull(companyId);
        if (activeCount <= 1) {
            throw CompanyException.lastBranch();
        }

        var branch = branchRepository.findByIdAndCompanyIdAndDeletedAtIsNull(branchId, companyId)
                .orElseThrow(() -> CompanyException.branchNotFound(branchId));
        branch.setDeletedAt(Instant.now());
        branchRepository.save(branch);
    }

    static BranchDto toDto(Branch branch) {
        return new BranchDto(
                branch.getId(),
                branch.getCompanyId(),
                branch.getName(),
                branch.getTimezone(),
                branch.getTaxZone(),
                branch.getCurrency(),
                branch.getAddress(),
                branch.getOpeningHours(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }
}
