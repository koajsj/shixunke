package com.example.demo.service.impl;

import com.example.demo.entity.Car;
import com.example.demo.entity.CarStatus;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Rental;
import com.example.demo.entity.RentalStatus;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RentalRepository;
import com.example.demo.service.interfaces.RentalService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class RentalServiceImpl implements RentalService {

    private static final List<RentalStatus> ACTIVE_RENTAL_STATUSES = List.of(
            RentalStatus.PENDING_PAYMENT,
            RentalStatus.RENTING
    );

    private final RentalRepository rentalRepository;
    private final CustomerRepository customerRepository;
    private final CarRepository carRepository;
    private final PaymentRepository paymentRepository;

    public RentalServiceImpl(
            RentalRepository rentalRepository,
            CustomerRepository customerRepository,
            CarRepository carRepository,
            PaymentRepository paymentRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.customerRepository = customerRepository;
        this.carRepository = carRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rental> findAll() {
        List<Rental> rentals = rentalRepository.findAll(Sort.by(Sort.Direction.ASC, "rentalId"));
        rentals.forEach(this::loadReferences);
        return rentals;
    }

    @Override
    @Transactional(readOnly = true)
    public Rental findById(Long id) {
        Rental rental = getRental(id);
        loadReferences(rental);
        return rental;
    }

    @Override
    public Rental create(Rental request) {
        Customer customer = getCustomer(request.getCustomerId());
        Car car = getCar(request.getCarId());
        validateRentalRequest(request, car, null);
        Rental savedRental = rentalRepository.save(buildRental(request, customer, car));
        refreshCarStatus(car);
        loadReferences(savedRental);
        return savedRental;
    }

    @Override
    public Rental update(Long id, Rental request) {
        Rental rental = getRental(id);
        Car previousCar = rental.getCar();
        Customer customer = getCustomer(request.getCustomerId());
        Car car = getCar(request.getCarId());
        validateRentalRequest(request, car, rental);
        applyRequest(rental, request, customer, car);
        Rental savedRental = rentalRepository.save(rental);
        if (!previousCar.getCarId().equals(car.getCarId())) {
            refreshCarStatus(previousCar);
        }
        refreshCarStatus(car);
        loadReferences(savedRental);
        return savedRental;
    }

    @Override
    public void delete(Long id) {
        Rental rental = getRental(id);
        if (paymentRepository.existsByRental(rental)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Rental is referenced by payment records and cannot be deleted."
            );
        }
        Car car = rental.getCar();
        rentalRepository.delete(rental);
        refreshCarStatus(car);
    }

    private Rental getRental(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rental not found: " + id));
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + id));
    }

    private Car getCar(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found: " + id));
    }

    private Rental buildRental(Rental request, Customer customer, Car car) {
        return new Rental(
                customer,
                car,
                request.getRentalDate(),
                request.getReturnDate(),
                request.getActualReturnDate(),
                request.getTotalAmount(),
                request.getStatus()
        );
    }

    private void validateRentalRequest(Rental request, Car car, Rental currentRental) {
        validateRentalOverlap(request, car, currentRental);

        if (isActiveStatus(request.getStatus())) {
            validateActiveRentalState(car, currentRental);
        }

        validateActualReturnDate(request);
    }

    private void validateRentalOverlap(Rental request, Car car, Rental currentRental) {
        if (request.getStatus() == RentalStatus.CANCELED) {
            return;
        }

        boolean overlapExists = rentalRepository.findByCar(car).stream()
                .filter(rental -> currentRental == null || !rental.getRentalId().equals(currentRental.getRentalId()))
                .filter(rental -> rental.getStatus() != RentalStatus.CANCELED)
                .anyMatch(rental -> isOverlapping(rental, request));

        if (overlapExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Car already has an overlapping rental during this period."
            );
        }
    }

    private boolean isActiveStatus(RentalStatus status) {
        return ACTIVE_RENTAL_STATUSES.contains(status);
    }

    private void applyRequest(Rental rental, Rental request, Customer customer, Car car) {
        rental.setCustomer(customer);
        rental.setCar(car);
        rental.setRentalDate(request.getRentalDate());
        rental.setReturnDate(request.getReturnDate());
        rental.setActualReturnDate(request.getActualReturnDate());
        rental.setTotalAmount(request.getTotalAmount());
        rental.setStatus(request.getStatus());
    }

    private void validateActiveRentalState(Car car, Rental currentRental) {
        if (hasOtherActiveRental(car, currentRental)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Car already has an active rental.");
        }
        if (car.getStatus() != CarStatus.AVAILABLE && isDifferentCar(car, currentRental)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Car is not available for rental.");
        }
    }

    private boolean hasOtherActiveRental(Car car, Rental currentRental) {
        return rentalRepository.findByCar(car).stream()
                .filter(rental -> currentRental == null || !rental.getRentalId().equals(currentRental.getRentalId()))
                .anyMatch(rental -> isActiveStatus(rental.getStatus()));
    }

    private boolean isDifferentCar(Car car, Rental currentRental) {
        return currentRental == null || !car.getCarId().equals(currentRental.getCar().getCarId());
    }

    private void validateActualReturnDate(Rental request) {
        if (request.getStatus() == RentalStatus.COMPLETED && request.getActualReturnDate() == null) {
            throw new IllegalArgumentException("Completed rental must include an actual return date.");
        }
        if (request.getStatus() != RentalStatus.COMPLETED && request.getActualReturnDate() != null) {
            throw new IllegalArgumentException("Only completed rentals can include an actual return date.");
        }
    }

    private void refreshCarStatus(Car car) {
        boolean hasActiveRental = rentalRepository.findByCar(car).stream()
                .anyMatch(rental -> isActiveStatus(rental.getStatus()));
        if (hasActiveRental) {
            if (car.getStatus() != CarStatus.RENTED) {
                car.setStatus(CarStatus.RENTED);
                carRepository.save(car);
            }
            return;
        }

        if (car.getStatus() == CarStatus.RENTED) {
            car.setStatus(CarStatus.AVAILABLE);
            carRepository.save(car);
        }
    }

    private boolean isOverlapping(Rental rental, Rental request) {
        return !rental.getRentalDate().isAfter(request.getReturnDate())
                && !rental.getReturnDate().isBefore(request.getRentalDate());
    }

    private void loadReferences(Rental rental) {
        rental.getCustomer().getCustomerId();
        rental.getCar().getCarId();
    }
}
