package com.novapos.inventory.repository;

import com.novapos.inventory.domain.TransferLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferLineRepository extends JpaRepository<TransferLine, UUID> {
    List<TransferLine> findByTransferOrderId(UUID transferOrderId);
}
