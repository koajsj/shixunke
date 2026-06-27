package com.example.demo.repository;

import com.example.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByLicenseNumber(String licenseNumber);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndCustomerIdNot(String phone, Long customerId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndCustomerIdNot(String email, Long customerId);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumberAndCustomerIdNot(String licenseNumber, Long customerId);
}
