package com.example.demo.service.impl;

import com.example.demo.entity.Payment;
import com.example.demo.entity.Rental;
import com.example.demo.entity.RentalStatus;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RentalRepository;
import com.example.demo.service.interfaces.PaymentService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, RentalRepository rentalRepository) {
        this.paymentRepository = paymentRepository;
        this.rentalRepository = rentalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        List<Payment> payments = paymentRepository.findAll(Sort.by(Sort.Direction.ASC, "paymentId"));
        payments.forEach(this::loadReferences);
        return payments;
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findById(Long id) {
        Payment payment = getPayment(id);
        loadReferences(payment);
        return payment;
    }

    @Override
    public Payment create(Payment request) {
        Rental rental = getRental(request.getRentalId());
        validatePaymentRequest(request, rental, null);
        Payment payment = new Payment(
                rental,
                request.getPaymentDate(),
                request.getAmount(),
                request.getPaymentMethod()
        );
        Payment savedPayment = paymentRepository.save(payment);
        refreshRentalStatusAfterPayment(rental);
        loadReferences(savedPayment);
        return savedPayment;
    }

    @Override
    public Payment update(Long id, Payment request) {
        Payment payment = getPayment(id);
        Rental rental = getRental(request.getRentalId());
        Rental previousRental = payment.getRental();
        validatePaymentRequest(request, rental, id);
        applyRequest(payment, request, rental);
        Payment savedPayment = paymentRepository.save(payment);
        refreshRentalStatusAfterPayment(previousRental);
        refreshRentalStatusAfterPayment(rental);
        loadReferences(savedPayment);
        return savedPayment;
    }

    @Override
    public void delete(Long id) {
        Payment payment = getPayment(id);
        Rental rental = payment.getRental();
        validateRentalAllowsPaymentChange(rental);
        paymentRepository.delete(payment);
        refreshRentalStatusAfterPayment(rental);
    }

    private Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: " + id));
    }

    private Rental getRental(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found: " + id));
    }

    private void validatePaymentRequest(Payment request, Rental rental, Long currentPaymentId) {
        validateRentalAllowsPaymentChange(rental);
        if (rental.getStatus() == RentalStatus.CANCELED) {
            throw new IllegalArgumentException("Canceled rental cannot accept payments.");
        }
        if (request.getPaymentDate().isBefore(rental.getRentalDate())) {
            throw new IllegalArgumentException("Payment date cannot be earlier than rental date.");
        }

        BigDecimal existingAmount = sumOtherPayments(rental, currentPaymentId);
        BigDecimal totalAfterPayment = existingAmount.add(request.getAmount());
        if (totalAfterPayment.compareTo(rental.getTotalAmount()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment exceeds the rental total amount.");
        }
    }

    private void validateRentalAllowsPaymentChange(Rental rental) {
        if (rental.getStatus() == RentalStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Completed rental payments cannot be modified."
            );
        }
    }

    private void applyRequest(Payment payment, Payment request, Rental rental) {
        payment.setRental(rental);
        payment.setPaymentDate(request.getPaymentDate());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
    }

    private BigDecimal sumOtherPayments(Rental rental, Long currentPaymentId) {
        return paymentRepository.findByRental(rental).stream()
                .filter(payment -> currentPaymentId == null || !payment.getPaymentId().equals(currentPaymentId))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void refreshRentalStatusAfterPayment(Rental rental) {
        BigDecimal paidAmount = paymentRepository.findByRental(rental).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (rental.getStatus() == RentalStatus.CANCELED || rental.getStatus() == RentalStatus.COMPLETED) {
            return;
        }

        RentalStatus nextStatus = paidAmount.compareTo(rental.getTotalAmount()) >= 0
                ? RentalStatus.RENTING
                : RentalStatus.PENDING_PAYMENT;
        rental.setStatus(nextStatus);
        rentalRepository.save(rental);
    }

    private void loadReferences(Payment payment) {
        payment.getRental().getRentalId();
    }
}
