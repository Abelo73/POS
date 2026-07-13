package com.novapos.customer.repository;

import com.novapos.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.Optional; import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByIdAndDeletedAtIsNull(UUID id);
    List<Customer> findByCompanyIdAndDeletedAtIsNull(UUID companyId);
}
