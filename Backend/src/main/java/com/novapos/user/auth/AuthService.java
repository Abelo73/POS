package com.novapos.user.auth;

import com.novapos.shared.security.AuthException;
import com.novapos.shared.security.JwtTokenProvider;
import com.novapos.shared.security.TotpUtil;
import com.novapos.user.repository.AppUserRepository;
import com.novapos.user.repository.RoleRepository;
import com.novapos.user.repository.TerminalRepository;
import com.novapos.user.repository.UserRoleAssignmentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository assignmentRepository;
    private final TerminalRepository terminalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AppUserRepository appUserRepository, RoleRepository roleRepository,
                       UserRoleAssignmentRepository assignmentRepository, TerminalRepository terminalRepository,
                       PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
        this.terminalRepository = terminalRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Map<String, String> login(String email, String password, String totpCode) {
        var user = appUserRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(AuthException::invalidCredentials);

        if (!user.isActive()) {
            throw AuthException.userNotActive();
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw AuthException.invalidCredentials();
        }

        if (user.isMfaEnabled() && user.getMfaSecret() != null) {
            if (totpCode == null || totpCode.isBlank()) {
                throw AuthException.mfaRequired();
            }
            if (!TotpUtil.verifyCode(user.getMfaSecret(), totpCode)) {
                throw AuthException.invalidMfaCode();
            }
        }

        var roles = assignmentRepository.findByUserId(user.getId()).stream()
                .map(a -> {
                    var role = roleRepository.findById(a.getRoleId());
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("roleId", a.getRoleId().toString());
                    roleMap.put("roleName", role.map(r -> (Object) r.getName()).orElse("UNKNOWN"));
                    if (a.getBranchId() != null) {
                        roleMap.put("branchId", a.getBranchId().toString());
                    }
                    return roleMap;
                })
                .toList();

        var accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getCompanyId(), roles);
        var refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public Map<String, String> refresh(String refreshToken) {
        try {
            var claims = jwtTokenProvider.validateToken(refreshToken);
            var userId = UUID.fromString(claims.getSubject());

            var user = appUserRepository.findByIdAndDeletedAtIsNull(userId)
                    .orElseThrow(AuthException::invalidToken);

            if (!user.isActive()) {
                throw AuthException.userNotActive();
            }

            var roles = assignmentRepository.findByUserId(user.getId()).stream()
                    .map(a -> {
                        var role = roleRepository.findById(a.getRoleId());
                        Map<String, Object> roleMap = new HashMap<>();
                        roleMap.put("roleId", a.getRoleId().toString());
                        roleMap.put("roleName", role.map(r -> (Object) r.getName()).orElse("UNKNOWN"));
                        if (a.getBranchId() != null) {
                            roleMap.put("branchId", a.getBranchId().toString());
                        }
                        return roleMap;
                    })
                    .toList();

            var newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getCompanyId(), roles);

            return Map.of("accessToken", newAccessToken);
        } catch (Exception e) {
            throw AuthException.invalidToken();
        }
    }

    public Map<String, String> pinLogin(UUID terminalId, String pinCode) {
        var terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> AuthException.invalidCredentials());

        var branchId = terminal.getBranchId();

        var assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getBranchId() != null && a.getBranchId().equals(branchId))
                .toList();

        var userWithPinMatch = assignments.stream()
                .map(a -> appUserRepository.findByIdAndDeletedAtIsNull(a.getUserId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(u -> u.isActive() && u.getPinHash() != null
                        && passwordEncoder.matches(pinCode, u.getPinHash()))
                .findFirst()
                .orElseThrow(AuthException::invalidCredentials);

        var roles = assignmentRepository.findByUserId(userWithPinMatch.getId()).stream()
                .map(a -> {
                    var role = roleRepository.findById(a.getRoleId());
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("roleId", a.getRoleId().toString());
                    roleMap.put("roleName", role.map(r -> (Object) r.getName()).orElse("UNKNOWN"));
                    if (a.getBranchId() != null) {
                        roleMap.put("branchId", a.getBranchId().toString());
                    }
                    return roleMap;
                })
                .toList();

        var accessToken = jwtTokenProvider.generateAccessToken(
                userWithPinMatch.getId(), userWithPinMatch.getEmail(), userWithPinMatch.getCompanyId(), roles);
        var refreshToken = jwtTokenProvider.generateRefreshToken(userWithPinMatch.getId());

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }
}
