package com.novapos.company.service;

import com.novapos.company.domain.Branch;
import com.novapos.company.repository.BranchRepository;
import com.novapos.company.web.CompanyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchService branchService;

    @Test
    @DisplayName("Deleting the last branch of a company is rejected")
    void deleteLastBranchIsRejected() {
        var companyId = UUID.randomUUID();
        var branchId = UUID.randomUUID();

        when(branchRepository.countByCompanyIdAndDeletedAtIsNull(companyId)).thenReturn(1L);

        assertThatThrownBy(() -> branchService.deleteBranch(branchId, companyId))
                .isInstanceOf(CompanyException.class)
                .hasFieldOrPropertyWithValue("code", "LAST_BRANCH");

        verify(branchRepository, never()).findByIdAndCompanyIdAndDeletedAtIsNull(any(), any());
    }

    @Test
    @DisplayName("Deleting a branch when multiple exist succeeds")
    void deleteBranchWhenMultipleExistSucceeds() {
        var companyId = UUID.randomUUID();
        var branchId = UUID.randomUUID();
        var branch = new Branch(companyId, "Test Branch", "UTC", "DEFAULT", "USD");

        when(branchRepository.countByCompanyIdAndDeletedAtIsNull(companyId)).thenReturn(2L);
        when(branchRepository.findByIdAndCompanyIdAndDeletedAtIsNull(branchId, companyId))
                .thenReturn(Optional.of(branch));

        branchService.deleteBranch(branchId, companyId);

        assertThat(branch.getDeletedAt()).isNotNull();
        verify(branchRepository).save(branch);
    }

    @Test
    @DisplayName("Deleting a non-existent branch throws not found")
    void deleteNonExistentBranchThrowsNotFound() {
        var companyId = UUID.randomUUID();
        var branchId = UUID.randomUUID();

        when(branchRepository.countByCompanyIdAndDeletedAtIsNull(companyId)).thenReturn(2L);
        when(branchRepository.findByIdAndCompanyIdAndDeletedAtIsNull(branchId, companyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> branchService.deleteBranch(branchId, companyId))
                .isInstanceOf(CompanyException.class)
                .hasFieldOrPropertyWithValue("code", "BRANCH_NOT_FOUND");
    }
}
