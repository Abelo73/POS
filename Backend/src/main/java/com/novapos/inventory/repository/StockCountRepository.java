package com.novapos.inventory.repository;

import com.novapos.inventory.domain.StockCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockCountRepository extends JpaRepository<StockCount, UUID> {
}
