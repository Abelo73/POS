package com.novapos.customer.repository;

import com.novapos.customer.domain.LoyaltyLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List; import java.util.UUID;

public interface LoyaltyLedgerRepository extends JpaRepository<LoyaltyLedger, UUID> {
    List<LoyaltyLedger> findByCustomerId(UUID customerId);
    @Query("select coalesce(sum(ll.pointsDelta), 0) from LoyaltyLedger ll where ll.customerId = :customerId")
    int sumPointsDelta(UUID customerId);
}
