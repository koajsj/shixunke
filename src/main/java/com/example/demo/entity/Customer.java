package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "Customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerID")
    private Long customerId;

    @Column(name = "Name", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String name;

    @Column(name = "Phone", nullable = false, unique = true, length = 20)
    @NotBlank
    @Size(max = 20)
    private String phone;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @Column(name = "LicenseNumber", nullable = false, unique = true, length = 30)
    @NotBlank
    @Size(max = 30)
    private String licenseNumber;

    public Customer() {
    }

    public Customer(String name, String phone, String email, String licenseNumber) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.licenseNumber = licenseNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalizeText(name);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = normalizePhone(phone);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = normalizeLicenseNumber(licenseNumber);
    }

    @PrePersist
    @PreUpdate
    void normalizeState() {
        name = normalizeText(name);
        phone = normalizePhone(phone);
        email = normalizeEmail(email);
        licenseNumber = normalizeLicenseNumber(licenseNumber);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizePhone(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private String normalizeLicenseNumber(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
