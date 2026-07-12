package com.novapos.user.service;

import com.novapos.user.domain.AppUser;
import com.novapos.user.repository.AppUserRepository;
import com.novapos.user.repository.RoleRepository;
import com.novapos.user.repository.UserRoleAssignmentRepository;
import com.novapos.user.web.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleAssignmentRepository assignmentRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Creating user with existing email throws conflict")
    void duplicateEmailThrowsConflict() {
        var companyId = UUID.randomUUID();
        when(appUserRepository.findByEmailAndDeletedAtIsNull("test@test.com"))
                .thenReturn(Optional.of(new AppUser(companyId, "test@test.com")));

        assertThatThrownBy(() -> userService.createUser(companyId, "test@test.com", null))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("code", "EMAIL_EXISTS");

        verify(appUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("Assigning non-existent role throws not found")
    void nonExistentRoleThrowsNotFound() {
        var userId = UUID.randomUUID();
        when(appUserRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(new AppUser(UUID.randomUUID(), "user@test.com")));
        when(roleRepository.findByName("NONEXISTENT"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRole(userId, "NONEXISTENT", null))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("code", "ROLE_NOT_FOUND");
    }

    @Test
    @DisplayName("Assigning role to non-existent user throws not found")
    void assignRoleToNonExistentUserThrowsNotFound() {
        var userId = UUID.randomUUID();
        when(appUserRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRole(userId, "CASHIER", null))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("code", "USER_NOT_FOUND");
    }

    @Test
    @DisplayName("Creating user successfully returns DTO")
    void createUserSuccess() {
        var companyId = UUID.randomUUID();
        var user = new AppUser(companyId, "new@test.com");

        when(appUserRepository.findByEmailAndDeletedAtIsNull("new@test.com"))
                .thenReturn(Optional.empty());
        when(appUserRepository.save(any(AppUser.class))).thenReturn(user);

        var dto = userService.createUser(companyId, "new@test.com", null);

        assertThat(dto.email()).isEqualTo("new@test.com");
        assertThat(dto.companyId()).isEqualTo(companyId);
        assertThat(dto.roles()).isEmpty();
    }
}
