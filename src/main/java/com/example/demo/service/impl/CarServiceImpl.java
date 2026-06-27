package com.example.demo.service.impl;

import com.example.demo.entity.Car;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.RentalRepository;
import com.example.demo.service.interfaces.CarService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;

    public CarServiceImpl(CarRepository carRepository, RentalRepository rentalRepository) {
        this.carRepository = carRepository;
        this.rentalRepository = rentalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> findAll() {
        return carRepository.findAll(Sort.by(Sort.Direction.ASC, "carId"));
    }

    @Override
    @Transactional(readOnly = true)
    public Car findById(Long id) {
        return getCar(id);
    }

    @Override
    public Car create(Car request) {
        String normalizedPlateNumber = normalizePlateNumber(request.getPlateNumber());
        validateUniquePlateNumber(normalizedPlateNumber, null);

        Car car = new Car(
                normalizedPlateNumber,
                request.getBrand(),
                request.getModel(),
                request.getColor(),
                request.getYear(),
                request.getStatus(),
                request.getDailyRate()
        );
        return carRepository.save(car);
    }

    @Override
    public Car update(Long id, Car request) {
        Car car = getCar(id);
        String normalizedPlateNumber = normalizePlateNumber(request.getPlateNumber());
        validateUniquePlateNumber(normalizedPlateNumber, id);
        car.setPlateNumber(normalizedPlateNumber);
        car.setBrand(request.getBrand());
        car.setModel(request.getModel());
        car.setColor(request.getColor());
        car.setYear(request.getYear());
        car.setStatus(request.getStatus());
        car.setDailyRate(request.getDailyRate());
        return carRepository.save(car);
    }

    @Override
    public void delete(Long id) {
        Car car = getCar(id);
        if (rentalRepository.existsByCar(car)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Car is referenced by rental records and cannot be deleted."
            );
        }
        carRepository.delete(car);
    }

    private void validateUniquePlateNumber(String plateNumber, Long currentCarId) {
        boolean exists = currentCarId == null
                ? carRepository.existsByPlateNumber(plateNumber)
                : carRepository.existsByPlateNumberAndCarIdNot(plateNumber, currentCarId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plate number already exists.");
        }
    }

    private String normalizePlateNumber(String plateNumber) {
        return plateNumber == null ? null : plateNumber.trim().toUpperCase();
    }

    private Car getCar(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found: " + id));
    }
}
