package com.novapos.user.repository;

import com.novapos.user.domain.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TerminalRepository extends JpaRepository<Terminal, UUID> {
}
