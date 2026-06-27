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
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RentalID", nullable = false)
    @NotNull
    @JsonIgnore
    private Rental rental;

    @Column(name = "PaymentDate", nullable = false)
    @NotNull
    private LocalDate paymentDate;

    @Column(name = "Amount", nullable = false, precision = 10, scale = 2)
    @NotNull
    @Positive
    private BigDecimal amount;

    @Column(name = "PaymentMethod", nullable = false, length = 20)
    @NotNull
    private String paymentMethodValue;

    public Payment() {
    }

    public Payment(Rental rental, LocalDate paymentDate, BigDecimal amount, PaymentMethod paymentMethod) {
        this.rental = rental;
        this.paymentDate = paymentDate;
        this.amount = amount;
        setPaymentMethod(paymentMethod);
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Rental getRental() {
        return rental;
    }

    public void setRental(Rental rental) {
        this.rental = rental;
    }

    @Transient
    public Long getRentalId() {
        return rental == null ? null : rental.getRentalId();
    }

    public void setRentalId(Long rentalId) {
        if (rentalId == null) {
            rental = null;
            return;
        }
        if (rental == null) {
            rental = new Rental();
        }
        rental.setRentalId(rentalId);
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethodValue == null ? null : PaymentMethod.fromPersistenceValue(paymentMethodValue);
    }

    @Transient
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethodValue = paymentMethod == null ? null : paymentMethod.name();
    }

    @PrePersist
    @PreUpdate
    void validateState() {
        if (amount != null && amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        if (rental != null && paymentDate != null && rental.getRentalDate() != null && paymentDate.isBefore(rental.getRentalDate())) {
            throw new IllegalArgumentException("Payment date cannot be earlier than rental date.");
        }
        if (rental != null && rental.getStatus() == RentalStatus.CANCELED) {
            throw new IllegalArgumentException("Canceled rental cannot accept payments.");
        }
        if (paymentMethodValue != null) {
            paymentMethodValue = getPaymentMethod().name();
        }
    }
}
