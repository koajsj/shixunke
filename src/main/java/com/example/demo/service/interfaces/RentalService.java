package com.example.demo.service.interfaces;

import com.example.demo.entity.Rental;

import java.util.List;

public interface RentalService {

    List<Rental> findAll();

    Rental findById(Long id);

    Rental create(Rental rental);

    Rental update(Long id, Rental rental);

    void delete(Long id);
}
