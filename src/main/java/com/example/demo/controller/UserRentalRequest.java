package com.example.demo.controller;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class UserRentalRequest {

    @NotNull
    private Long carId;

    @NotNull
    private LocalDate rentalDate;

    @NotNull
    private LocalDate returnDate;

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public LocalDate getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(LocalDate rentalDate) {
        this.rentalDate = rentalDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}
