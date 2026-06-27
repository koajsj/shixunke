package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Year;

@Entity
@Table(name = "Car")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CarID")
    private Long carId;

    @Column(name = "PlateNumber", nullable = false, unique = true, length = 20)
    @NotBlank
    @Size(max = 20)
    private String plateNumber;

    @Column(name = "Brand", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String brand;

    @Column(name = "Model", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String model;

    @Column(name = "Color", nullable = false, length = 20)
    @NotBlank
    @Size(max = 20)
    private String color;

    @Column(name = "Year", nullable = false)
    @NotNull
    @Min(1886)
    @Max(3000)
    private Integer year;

    @Column(name = "Status", nullable = false, length = 20)
    @NotNull
    private String statusValue;

    @Column(name = "DailyRate", nullable = false, precision = 10, scale = 2)
    @NotNull
    @Positive
    private BigDecimal dailyRate;

    public Car() {
    }

    public Car(String plateNumber, String brand, String model, String color, Integer year, CarStatus status, BigDecimal dailyRate) {
        this.plateNumber = plateNumber;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.year = year;
        setStatus(status);
        this.dailyRate = dailyRate;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = normalizePlateNumber(plateNumber);
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = normalizeText(brand);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = normalizeText(model);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = normalizeText(color);
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public CarStatus getStatus() {
        return statusValue == null ? null : CarStatus.fromPersistenceValue(statusValue);
    }

    @Transient
    public void setStatus(CarStatus status) {
        this.statusValue = status == null ? null : status.name();
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    @PrePersist
    @PreUpdate
    void validateState() {
        plateNumber = normalizePlateNumber(plateNumber);
        brand = normalizeText(brand);
        model = normalizeText(model);
        color = normalizeText(color);
        int currentYear = Year.now().getValue() + 1;
        if (year != null && (year < 1886 || year > currentYear)) {
            throw new IllegalArgumentException("Car year is out of range.");
        }
        if (dailyRate != null && dailyRate.signum() <= 0) {
            throw new IllegalArgumentException("Daily rate must be greater than zero.");
        }
        if (statusValue != null) {
            statusValue = getStatus().name();
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizePlateNumber(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
