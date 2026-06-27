package com.example.demo.repository;

import com.example.demo.entity.Car;
import com.example.demo.entity.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    Optional<Car> findByPlateNumber(String plateNumber);

    List<Car> findByStatusValueIn(List<String> statusValues);

    List<Car> findByStatusValue(String statusValue);

    default List<Car> findByStatus(CarStatus status) {
        return findByStatusValueIn(List.of(status.getDbValue(), status.name()));
    }

    boolean existsByPlateNumber(String plateNumber);

    boolean existsByPlateNumberAndCarIdNot(String plateNumber, Long carId);
}
