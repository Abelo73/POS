package com.novapos.user.repository;

import com.novapos.user.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByIdAndDeletedAtIsNull(UUID id);
    Optional<AppUser> findByEmailAndDeletedAtIsNull(String email);
}
