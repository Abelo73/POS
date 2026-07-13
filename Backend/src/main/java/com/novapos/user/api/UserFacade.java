package com.novapos.user.api;

import com.novapos.user.api.dto.AppUserDto;
import com.novapos.user.api.dto.RoleDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFacade {

    AppUserDto createUser(UUID companyId, String email, String phone);

    Optional<AppUserDto> getUser(UUID userId);

    void assignRole(UUID userId, String roleName, UUID branchId);

    List<AppUserDto> getUsersByCompany(UUID companyId);

    List<RoleDto> getAllRoles();

    void setPassword(UUID userId, String rawPassword);

    void setPin(UUID userId, String rawPin);
}
