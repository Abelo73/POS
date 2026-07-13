package com.novapos.pos.repository;

import com.novapos.pos.domain.SaleLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SaleLineRepository extends JpaRepository<SaleLine, UUID> {
    List<SaleLine> findBySaleId(UUID saleId);
}
