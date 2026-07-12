package com.novapos.company.service;

import com.novapos.company.api.CompanyFacade;
import com.novapos.company.api.dto.BranchDto;
import com.novapos.company.api.dto.CompanyDto;
import com.novapos.company.domain.Branch;
import com.novapos.company.domain.Company;
import com.novapos.company.repository.BranchRepository;
import com.novapos.company.repository.CompanyRepository;
import com.novapos.company.web.CompanyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
class CompanyService implements CompanyFacade {

    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;

    CompanyService(CompanyRepository companyRepository, BranchRepository branchRepository) {
        this.companyRepository = companyRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public CompanyDto createCompany(String name, String defaultCurrency, String defaultBranchName, String timezone, String taxZone, String currency) {
        var company = new Company(name, defaultCurrency);
        company = companyRepository.save(company);

        var branch = new Branch(company.getId(), defaultBranchName, timezone, taxZone, currency);
        branchRepository.save(branch);

        return toDto(company);
    }

    @Override
    public Optional<CompanyDto> getCompany(UUID companyId) {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .map(CompanyService::toDto);
    }

    @Override
    public CompanyDto updateCompany(UUID companyId, String name, String defaultCurrency) {
        var company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> CompanyException.notFound(companyId));
        if (name != null) {
            company.setName(name);
        }
        if (defaultCurrency != null) {
            company.setDefaultCurrency(defaultCurrency);
        }
        company.markUpdated();
        company = companyRepository.save(company);
        return toDto(company);
    }

    @Override
    public void deleteCompany(UUID companyId) {
        var company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                .orElseThrow(() -> CompanyException.notFound(companyId));
        var now = Instant.now();
        company.setDeletedAt(now);

        var branches = branchRepository.findByCompanyIdAndDeletedAtIsNull(companyId);
        for (var branch : branches) {
            branch.setDeletedAt(now);
        }
        companyRepository.save(company);
        branchRepository.saveAll(branches);
    }

    @Override
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .filter(c -> c.getDeletedAt() == null)
                .map(CompanyService::toDto)
                .toList();
    }

    static CompanyDto toDto(Company company) {
        return new CompanyDto(
                company.getId(),
                company.getName(),
                company.getDefaultCurrency(),
                company.getBillingStatus(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
