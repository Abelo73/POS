package com.novapos.inventory.repository;

import com.novapos.inventory.domain.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BatchRepository extends JpaRepository<Batch, UUID> {

    List<Batch> findByProductVariantId(UUID productVariantId);

    List<Batch> findByProductVariantIdAndExpiryDateBefore(UUID productVariantId, LocalDate date);
}
