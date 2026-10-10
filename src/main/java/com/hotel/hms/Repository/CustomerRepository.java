
package com.hotel.hms.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hms.Entity.Customer;

public interface CustomerRepository
        extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByEmailIgnoreCase(String email);
}
