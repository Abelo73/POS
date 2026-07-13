package com.novapos.inventory.repository;

import com.novapos.inventory.domain.StockCountLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockCountLineRepository extends JpaRepository<StockCountLine, UUID> {
    List<StockCountLine> findByStockCountId(UUID stockCountId);
}
