package com.novapos.company.api;

import com.novapos.company.api.dto.BranchDto;
import com.novapos.company.api.dto.CompanyDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyFacade {

    CompanyDto createCompany(String name, String defaultCurrency, String defaultBranchName, String timezone, String taxZone, String currency);

    Optional<CompanyDto> getCompany(UUID companyId);

    CompanyDto updateCompany(UUID companyId, String name, String defaultCurrency);

    void deleteCompany(UUID companyId);

    List<CompanyDto> getAllCompanies();
}
