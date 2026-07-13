package com.novapos.inventory.repository;

import com.novapos.inventory.domain.TransferOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferOrderRepository extends JpaRepository<TransferOrder, UUID> {
    List<TransferOrder> findBySourceLocationIdOrDestinationLocationId(UUID sourceLocationId, UUID destinationLocationId);
}
