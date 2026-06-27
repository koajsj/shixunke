package com.example.demo.repository;

import com.example.demo.entity.Payment;
import com.example.demo.entity.PaymentMethod;
import com.example.demo.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByRental(Rental rental);

    List<Payment> findByPaymentMethodValueIn(List<String> paymentMethodValues);

    List<Payment> findByPaymentMethodValue(String paymentMethodValue);

    default List<Payment> findByPaymentMethod(PaymentMethod paymentMethod) {
        return findByPaymentMethodValueIn(List.of(paymentMethod.getDbValue(), paymentMethod.name()));
    }

    boolean existsByRental(Rental rental);
}
