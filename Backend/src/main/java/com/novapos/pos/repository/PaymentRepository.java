package com.novapos.pos.repository;

import com.novapos.pos.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findBySaleId(UUID saleId);
}
