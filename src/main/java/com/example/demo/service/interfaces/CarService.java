package com.example.demo.service.interfaces;

import com.example.demo.entity.Car;

import java.util.List;

public interface CarService {

    List<Car> findAll();

    Car findById(Long id);

    Car create(Car car);

    Car update(Long id, Car car);

    void delete(Long id);
}
