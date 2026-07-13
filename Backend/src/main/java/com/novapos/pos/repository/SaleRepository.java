package com.novapos.pos.repository;

import com.novapos.pos.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
    Optional<Sale> findByClientUuid(UUID clientUuid);
}
