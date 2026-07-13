package com.novapos.user.service;

import com.novapos.user.api.UserFacade;
import com.novapos.user.api.dto.AppUserDto;
import com.novapos.user.api.dto.RoleAssignmentDto;
import com.novapos.user.api.dto.RoleDto;
import com.novapos.user.domain.AppUser;
import com.novapos.user.domain.Role;
import com.novapos.user.domain.UserRoleAssignment;
import com.novapos.user.repository.AppUserRepository;
import com.novapos.user.repository.RoleRepository;
import com.novapos.user.repository.UserRoleAssignmentRepository;
import com.novapos.user.web.UserException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
class UserService implements UserFacade {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    UserService(AppUserRepository appUserRepository, RoleRepository roleRepository,
                UserRoleAssignmentRepository assignmentRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AppUserDto createUser(UUID companyId, String email, String phone) {
        if (appUserRepository.findByEmailAndDeletedAtIsNull(email).isPresent()) {
            throw UserException.emailAlreadyExists(email);
        }
        var user = new AppUser(companyId, email);
        if (phone != null) {
            user.setPhone(phone);
        }
        user = appUserRepository.save(user);
        return toDto(user, List.of());
    }

    @Override
    public Optional<AppUserDto> getUser(UUID userId) {
        return appUserRepository.findByIdAndDeletedAtIsNull(userId)
                .map(user -> toDto(user, assignmentRepository.findByUserId(userId)));
    }

    @Override
    public void assignRole(UUID userId, String roleName, UUID branchId) {
        var user = appUserRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> UserException.notFound(userId));
        var role = roleRepository.findByName(roleName)
                .orElseThrow(() -> UserException.roleNotFound(roleName));
        var assignment = new UserRoleAssignment(user.getId(), role.getId(), branchId);
        assignmentRepository.save(assignment);
    }

    @Override
    public List<AppUserDto> getUsersByCompany(UUID companyId) {
        return appUserRepository.findAll().stream()
                .filter(u -> u.getDeletedAt() == null && u.getCompanyId().equals(companyId))
                .map(user -> toDto(user, assignmentRepository.findByUserId(user.getId())))
                .toList();
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(r -> new RoleDto(r.getId(), r.getName(), r.getDescription()))
                .toList();
    }

    @Override
    public void setPassword(UUID userId, String rawPassword) {
        var user = appUserRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> UserException.notFound(userId));
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        appUserRepository.save(user);
    }

    @Override
    public void setPin(UUID userId, String rawPin) {
        var user = appUserRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> UserException.notFound(userId));
        user.setPinHash(passwordEncoder.encode(rawPin));
        appUserRepository.save(user);
    }

    private AppUserDto toDto(AppUser user, List<UserRoleAssignment> assignments) {
        return new AppUserDto(
                user.getId(),
                user.getCompanyId(),
                user.getEmail(),
                user.getPhone(),
                user.isMfaEnabled(),
                user.isActive(),
                assignments.stream().map(a -> {
                    var role = roleRepository.findById(a.getRoleId()).orElse(null);
                    return new RoleAssignmentDto(a.getRoleId(), role != null ? role.getName() : "UNKNOWN", a.getBranchId());
                }).toList(),
                user.getCreatedAt()
        );
    }
}
