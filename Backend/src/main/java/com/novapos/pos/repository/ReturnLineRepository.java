package com.novapos.pos.repository;

import com.novapos.pos.domain.ReturnLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReturnLineRepository extends JpaRepository<ReturnLine, UUID> {
    List<ReturnLine> findByOriginalSaleLineId(UUID originalSaleLineId);
    List<ReturnLine> findByReturnSaleId(UUID returnSaleId);
}
