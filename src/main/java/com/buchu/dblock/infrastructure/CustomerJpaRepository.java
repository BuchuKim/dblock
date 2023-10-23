package com.buchu.dblock.infrastructure;

import com.buchu.dblock.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findByName(String name);
}
