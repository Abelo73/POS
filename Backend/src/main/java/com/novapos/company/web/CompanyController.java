package com.novapos.company.web;

import com.novapos.company.api.CompanyFacade;
import com.novapos.company.api.dto.CompanyDto;
import com.novapos.company.web.dto.CreateCompanyRequest;
import com.novapos.company.web.dto.UpdateCompanyRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
class CompanyController {

    private final CompanyFacade companyFacade;

    CompanyController(CompanyFacade companyFacade) {
        this.companyFacade = companyFacade;
    }

    @PostMapping
    ResponseEntity<CompanyDto> create(@Valid @RequestBody CreateCompanyRequest request) {
        var company = companyFacade.createCompany(
                request.name(), request.defaultCurrency(),
                request.defaultBranchName(), request.timezone(),
                request.taxZone(), request.currency());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(company.id())
                .toUri();
        return ResponseEntity.created(location).body(company);
    }

    @GetMapping("/{companyId}")
    ResponseEntity<CompanyDto> getById(@PathVariable UUID companyId) {
        return companyFacade.getCompany(companyId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> CompanyException.notFound(companyId));
    }

    @GetMapping
    ResponseEntity<List<CompanyDto>> getAll() {
        return ResponseEntity.ok(companyFacade.getAllCompanies());
    }

    @PutMapping("/{companyId}")
    ResponseEntity<CompanyDto> update(@PathVariable UUID companyId, @Valid @RequestBody UpdateCompanyRequest request) {
        var company = companyFacade.updateCompany(companyId, request.name(), request.defaultCurrency());
        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{companyId}")
    ResponseEntity<Void> delete(@PathVariable UUID companyId) {
        companyFacade.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
