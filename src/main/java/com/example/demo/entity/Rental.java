package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Rental")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RentalID")
    private Long rentalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CustomerID", nullable = false)
    @NotNull
    @JsonIgnore
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CarID", nullable = false)
    @NotNull
    @JsonIgnore
    private Car car;

    @Column(name = "RentalDate", nullable = false)
    @NotNull
    private LocalDate rentalDate;

    @Column(name = "ReturnDate", nullable = false)
    @NotNull
    private LocalDate returnDate;

    @Column(name = "ActualReturnDate")
    private LocalDate actualReturnDate;

    @Column(name = "TotalAmount", nullable = false, precision = 10, scale = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal totalAmount;

    @Column(name = "Status", nullable = false, length = 20)
    @NotNull
    private String statusValue;

    public Rental() {
    }

    public Rental(Customer customer, Car car, LocalDate rentalDate, LocalDate returnDate, LocalDate actualReturnDate, BigDecimal totalAmount, RentalStatus status) {
        this.customer = customer;
        this.car = car;
        this.rentalDate = rentalDate;
        this.returnDate = returnDate;
        this.actualReturnDate = actualReturnDate;
        this.totalAmount = totalAmount;
        setStatus(status);
    }

    public Long getRentalId() {
        return rentalId;
    }

    public void setRentalId(Long rentalId) {
        this.rentalId = rentalId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Transient
    public Long getCustomerId() {
        return customer == null ? null : customer.getCustomerId();
    }

    public void setCustomerId(Long customerId) {
        if (customerId == null) {
            customer = null;
            return;
        }
        if (customer == null) {
            customer = new Customer();
        }
        customer.setCustomerId(customerId);
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    @Transient
    public Long getCarId() {
        return car == null ? null : car.getCarId();
    }

    public void setCarId(Long carId) {
        if (carId == null) {
            car = null;
            return;
        }
        if (car == null) {
            car = new Car();
        }
        car.setCarId(carId);
    }

    public LocalDate getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(LocalDate rentalDate) {
        this.rentalDate = rentalDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public RentalStatus getStatus() {
        return statusValue == null ? null : RentalStatus.fromPersistenceValue(statusValue);
    }

    @Transient
    public void setStatus(RentalStatus status) {
        this.statusValue = status == null ? null : status.name();
    }

    @PrePersist
    @PreUpdate
    void validateState() {
        if (rentalDate != null && returnDate != null && returnDate.isBefore(rentalDate)) {
            throw new IllegalArgumentException("Return date cannot be earlier than rental date.");
        }
        if (rentalDate != null && actualReturnDate != null && actualReturnDate.isBefore(rentalDate)) {
            throw new IllegalArgumentException("Actual return date cannot be earlier than rental date.");
        }
        if (totalAmount != null && totalAmount.signum() < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative.");
        }
        RentalStatus currentStatus = getStatus();
        if (currentStatus == RentalStatus.COMPLETED && actualReturnDate == null) {
            throw new IllegalArgumentException("Completed rental must include an actual return date.");
        }
        if (currentStatus != null && currentStatus != RentalStatus.COMPLETED && actualReturnDate != null) {
            throw new IllegalArgumentException("Only completed rentals can include an actual return date.");
        }
        if (statusValue != null) {
            statusValue = currentStatus.name();
        }
    }
}
