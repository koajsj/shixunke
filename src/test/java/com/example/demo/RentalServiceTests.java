package com.example.demo;

import com.example.demo.entity.Car;
import com.example.demo.entity.CarStatus;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Rental;
import com.example.demo.entity.RentalStatus;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.RentalRepository;
import com.example.demo.service.interfaces.RentalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class RentalServiceTests {

    @Autowired
    private RentalService rentalService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Test
    void shouldCreateRentalAndMarkCarAsRented() {
        Customer customer = customerRepository.save(new Customer(
                "Service User",
                "13100000001",
                "service-user@example.com",
                "LIC101"
        ));
        Car car = carRepository.save(new Car(
                "S10001",
                "Toyota",
                "Corolla",
                "White",
                2024,
                CarStatus.AVAILABLE,
                new BigDecimal("200.00")
        ));

        Rental request = new Rental();
        request.setCustomerId(customer.getCustomerId());
        request.setCarId(car.getCarId());
        request.setRentalDate(LocalDate.of(2026, 6, 20));
        request.setReturnDate(LocalDate.of(2026, 6, 22));
        request.setActualReturnDate(null);
        request.setTotalAmount(new BigDecimal("400.00"));
        request.setStatus(RentalStatus.PENDING_PAYMENT);

        Rental response = rentalService.create(request);

        Car savedCar = carRepository.findById(car.getCarId()).orElseThrow();
        assertEquals(car.getCarId(), response.getCarId());
        assertEquals(RentalStatus.PENDING_PAYMENT, response.getStatus());
        assertEquals(CarStatus.RENTED, savedCar.getStatus());
    }

    @Test
    void shouldRejectRentalForMaintenanceCar() {
        Customer customer = customerRepository.save(new Customer(
                "Service Blocked",
                "13100000002",
                "service-blocked@example.com",
                "LIC102"
        ));
        Car car = carRepository.save(new Car(
                "S10002",
                "Honda",
                "Civic",
                "Black",
                2023,
                CarStatus.MAINTENANCE,
                new BigDecimal("180.00")
        ));

        Rental request = new Rental();
        request.setCustomerId(customer.getCustomerId());
        request.setCarId(car.getCarId());
        request.setRentalDate(LocalDate.of(2026, 6, 20));
        request.setReturnDate(LocalDate.of(2026, 6, 21));
        request.setActualReturnDate(null);
        request.setTotalAmount(new BigDecimal("180.00"));
        request.setStatus(RentalStatus.PENDING_PAYMENT);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                rentalService.create(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Car is not available for rental.", exception.getReason());
    }

    @Test
    void shouldDeleteRentalAndResetCarStatusWhenNoActiveRentalRemains() {
        Customer customer = customerRepository.save(new Customer(
                "Service Delete",
                "13100000003",
                "service-delete@example.com",
                "LIC103"
        ));
        Car car = carRepository.save(new Car(
                "S10003",
                "Nissan",
                "Sylphy",
                "Silver",
                2022,
                CarStatus.RENTED,
                new BigDecimal("160.00")
        ));
        Rental rental = rentalRepository.save(new Rental(
                customer,
                car,
                LocalDate.of(2026, 6, 20),
                LocalDate.of(2026, 6, 22),
                null,
                new BigDecimal("320.00"),
                RentalStatus.RENTING
        ));

        rentalService.delete(rental.getRentalId());

        Car savedCar = carRepository.findById(car.getCarId()).orElseThrow();
        assertFalse(rentalRepository.existsById(rental.getRentalId()));
        assertEquals(CarStatus.AVAILABLE, savedCar.getStatus());
    }
}
