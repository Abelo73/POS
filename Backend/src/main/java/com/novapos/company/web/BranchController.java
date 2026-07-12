package com.novapos.company.web;

import com.novapos.company.api.BranchFacade;
import com.novapos.company.api.dto.BranchDto;
import com.novapos.company.web.dto.CreateBranchRequest;
import com.novapos.company.web.dto.UpdateBranchRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/companies/{companyId}/branches")
class BranchController {

    private final BranchFacade branchFacade;

    BranchController(BranchFacade branchFacade) {
        this.branchFacade = branchFacade;
    }

    @PostMapping
    ResponseEntity<BranchDto> create(@PathVariable UUID companyId, @Valid @RequestBody CreateBranchRequest request) {
        var branch = branchFacade.createBranch(companyId,
                request.name(), request.timezone(), request.taxZone(), request.currency(),
                request.address(), request.openingHours());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(branch.id())
                .toUri();
        return ResponseEntity.created(location).body(branch);
    }

    @GetMapping("/{branchId}")
    ResponseEntity<BranchDto> getById(@PathVariable UUID companyId, @PathVariable UUID branchId) {
        return branchFacade.getBranch(branchId, companyId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> CompanyException.branchNotFound(branchId));
    }

    @GetMapping
    ResponseEntity<List<BranchDto>> getByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(branchFacade.getBranchesByCompany(companyId));
    }

    @PutMapping("/{branchId}")
    ResponseEntity<BranchDto> update(@PathVariable UUID companyId, @PathVariable UUID branchId, @Valid @RequestBody UpdateBranchRequest request) {
        var branch = branchFacade.updateBranch(branchId, companyId, request.name(), request.timezone(), request.taxZone(), request.currency());
        return ResponseEntity.ok(branch);
    }

    @DeleteMapping("/{branchId}")
    ResponseEntity<Void> delete(@PathVariable UUID companyId, @PathVariable UUID branchId) {
        branchFacade.deleteBranch(branchId, companyId);
        return ResponseEntity.noContent().build();
    }
}
