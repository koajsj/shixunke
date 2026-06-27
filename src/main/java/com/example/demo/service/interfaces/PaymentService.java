package com.example.demo.service.interfaces;

import com.example.demo.entity.Payment;

import java.util.List;

public interface PaymentService {

    List<Payment> findAll();

    Payment findById(Long id);

    Payment create(Payment payment);

    Payment update(Long id, Payment payment);

    void delete(Long id);
}
