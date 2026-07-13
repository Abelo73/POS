package com.novapos.catalog.repository;

import com.novapos.catalog.domain.BundleComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BundleComponentRepository extends JpaRepository<BundleComponent, UUID> {

    List<BundleComponent> findByBundleProductId(UUID bundleProductId);

    void deleteByBundleProductIdAndComponentProductId(UUID bundleProductId, UUID componentProductId);
}
