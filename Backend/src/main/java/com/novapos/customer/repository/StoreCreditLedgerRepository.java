package com.novapos.customer.repository;

import com.novapos.customer.domain.StoreCreditLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List; import java.util.UUID;

public interface StoreCreditLedgerRepository extends JpaRepository<StoreCreditLedger, UUID> {
    List<StoreCreditLedger> findByCustomerId(UUID customerId);
    @Query("select coalesce(sum(scl.amountDeltaMinor), 0) from StoreCreditLedger scl where scl.customerId = :customerId")
    long sumAmountDelta(UUID customerId);
}
