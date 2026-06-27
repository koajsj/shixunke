package com.example.demo.repository;

import com.example.demo.entity.Car;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Rental;
import com.example.demo.entity.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Collectors;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByCustomer(Customer customer);

    List<Rental> findByCar(Car car);

    List<Rental> findByStatusValueIn(List<String> statusValues);

    List<Rental> findByStatusValue(String statusValue);

    default List<Rental> findByStatus(RentalStatus status) {
        return findByStatusValueIn(toPersistenceValues(status));
    }

    boolean existsByCustomer(Customer customer);

    boolean existsByCar(Car car);

    boolean existsByCarAndStatusValueIn(Car car, List<String> statusValues);

    default boolean existsByCarAndStatusIn(Car car, List<RentalStatus> statuses) {
        return existsByCarAndStatusValueIn(car, toPersistenceValues(statuses));
    }

    boolean existsByCarAndStatusValueInAndRentalIdNot(Car car, List<String> statusValues, Long rentalId);

    default boolean existsByCarAndStatusInAndRentalIdNot(Car car, List<RentalStatus> statuses, Long rentalId) {
        return existsByCarAndStatusValueInAndRentalIdNot(car, toPersistenceValues(statuses), rentalId);
    }

    boolean existsByCarAndStatusValueNotAndRentalDateLessThanEqualAndReturnDateGreaterThanEqual(
            Car car,
            String statusValue,
            java.time.LocalDate returnDate,
            java.time.LocalDate rentalDate
    );

    default boolean existsByCarAndStatusNotAndRentalDateLessThanEqualAndReturnDateGreaterThanEqual(
            Car car,
            RentalStatus status,
            java.time.LocalDate returnDate,
            java.time.LocalDate rentalDate
    ) {
        return existsByCarAndStatusValueNotAndRentalDateLessThanEqualAndReturnDateGreaterThanEqual(
                car,
                status.getDbValue(),
                returnDate,
                rentalDate
        );
    }

    boolean existsByCarAndStatusValueNotAndRentalDateLessThanEqualAndReturnDateGreaterThanEqualAndRentalIdNot(
            Car car,
            String statusValue,
            java.time.LocalDate returnDate,
            java.time.LocalDate rentalDate,
            Long rentalId
    );

    default boolean existsByCarAndStatusNotAndRentalDateLessThanEqualAndReturnDateGreaterThanEqualAndRentalIdNot(
            Car car,
            RentalStatus status,
            java.time.LocalDate returnDate,
            java.time.LocalDate rentalDate,
            Long rentalId
    ) {
        return existsByCarAndStatusValueNotAndRentalDateLessThanEqualAndReturnDateGreaterThanEqualAndRentalIdNot(
                car,
                status.getDbValue(),
                returnDate,
                rentalDate,
                rentalId
        );
    }

    private static List<String> toPersistenceValues(List<RentalStatus> statuses) {
        return statuses.stream()
                .flatMap(status -> toPersistenceValues(status).stream())
                .collect(Collectors.toList());
    }

    private static List<String> toPersistenceValues(RentalStatus status) {
        return List.of(status.getDbValue(), status.name());
    }
}
