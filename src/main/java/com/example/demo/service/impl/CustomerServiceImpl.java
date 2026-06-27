package com.example.demo.service.impl;

import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.RentalRepository;
import com.example.demo.service.interfaces.CustomerService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RentalRepository rentalRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, RentalRepository rentalRepository) {
        this.customerRepository = customerRepository;
        this.rentalRepository = rentalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll(Sort.by(Sort.Direction.ASC, "customerId"));
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return getCustomer(id);
    }

    @Override
    public Customer create(Customer request) {
        String normalizedPhone = normalizePhone(request.getPhone());
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedLicenseNumber = normalizeLicenseNumber(request.getLicenseNumber());
        validateUniqueFields(normalizedPhone, normalizedEmail, normalizedLicenseNumber, null);

        Customer customer = new Customer(
                request.getName(),
                normalizedPhone,
                normalizedEmail,
                normalizedLicenseNumber
        );
        return customerRepository.save(customer);
    }

    @Override
    public Customer update(Long id, Customer request) {
        Customer customer = getCustomer(id);
        String normalizedPhone = normalizePhone(request.getPhone());
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedLicenseNumber = normalizeLicenseNumber(request.getLicenseNumber());
        validateUniqueFields(normalizedPhone, normalizedEmail, normalizedLicenseNumber, id);
        customer.setName(request.getName());
        customer.setPhone(normalizedPhone);
        customer.setEmail(normalizedEmail);
        customer.setLicenseNumber(normalizedLicenseNumber);
        return customerRepository.save(customer);
    }

    @Override
    public void delete(Long id) {
        Customer customer = getCustomer(id);
        if (rentalRepository.existsByCustomer(customer)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer is referenced by rental records and cannot be deleted."
            );
        }
        customerRepository.delete(customer);
    }

    private void validateUniqueFields(String phone, String email, String licenseNumber, Long currentCustomerId) {
        boolean phoneExists = currentCustomerId == null
                ? customerRepository.existsByPhone(phone)
                : customerRepository.existsByPhoneAndCustomerIdNot(phone, currentCustomerId);
        if (phoneExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already exists.");
        }

        boolean emailExists = currentCustomerId == null
                ? customerRepository.existsByEmail(email)
                : customerRepository.existsByEmailAndCustomerIdNot(email, currentCustomerId);
        if (emailExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }

        boolean licenseExists = currentCustomerId == null
                ? customerRepository.existsByLicenseNumber(licenseNumber)
                : customerRepository.existsByLicenseNumberAndCustomerIdNot(licenseNumber, currentCustomerId);
        if (licenseExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "License number already exists.");
        }
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeLicenseNumber(String licenseNumber) {
        return licenseNumber == null ? null : licenseNumber.trim().toUpperCase();
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + id));
    }
}
