package com.example.demo;

import com.example.demo.entity.Car;
import com.example.demo.entity.CarStatus;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Rental;
import com.example.demo.entity.RentalStatus;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldCreateAndFetchCar() throws Exception {
        mockMvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plateNumber": "A12345",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "color": "White",
                                  "year": 2024,
                                  "status": "AVAILABLE",
                                  "dailyRate": 199.99
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carId").isNumber())
                .andExpect(jsonPath("$.plateNumber").value("A12345"));

        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].plateNumber", hasItem("A12345")))
                .andExpect(jsonPath("$[*].brand", hasItem("Toyota")));
    }

    @Test
    void shouldPersistCarYearUsingSqlContractColumnName() {
        Car car = carRepository.save(new Car(
                "SQL123",
                "Toyota",
                "Yaris",
                "White",
                2024,
                CarStatus.AVAILABLE,
                new BigDecimal("188.00")
        ));

        try (Connection connection = dataSource.getConnection();
             ResultSet columns = connection.getMetaData().getColumns(null, null, "car", null)) {
            boolean hasYearColumn = false;
            boolean hasLegacyProductionYearColumn = false;
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if ("year".equalsIgnoreCase(columnName)) {
                    hasYearColumn = true;
                }
                if ("productionyear".equalsIgnoreCase(columnName)) {
                    hasLegacyProductionYearColumn = true;
                }
            }

            assertTrue(hasYearColumn);
            assertFalse(hasLegacyProductionYearColumn);
            assertEquals(2024, car.getYear());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void shouldRejectInvalidRentalDates() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "Alice",
                "13800000000",
                "alice@example.com",
                "LIC001"
        ));
        Car car = carRepository.save(new Car(
                "B12345",
                "Honda",
                "Civic",
                "Black",
                2023,
                CarStatus.AVAILABLE,
                new BigDecimal("180.00")
        ));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "carId": %d,
                                  "rentalDate": "2026-06-10",
                                  "returnDate": "2026-06-09",
                                  "actualReturnDate": null,
                                  "totalAmount": 500.00,
                                  "status": "PENDING_PAYMENT"
                                }
                                """.formatted(customer.getCustomerId(), car.getCarId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Return date cannot be earlier than rental date."));
    }

    @Test
    void shouldReturnNotFoundForMissingCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found: 999"));
    }

    @Test
    void shouldRejectDuplicatePlateNumber() throws Exception {
        carRepository.save(new Car(
                "D12345",
                "BYD",
                "Qin",
                "Gray",
                2024,
                CarStatus.AVAILABLE,
                new BigDecimal("150.00")
        ));

        mockMvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plateNumber": " d12345 ",
                                  "brand": "Tesla",
                                  "model": "Model 3",
                                  "color": "White",
                                  "year": 2024,
                                  "status": "AVAILABLE",
                                  "dailyRate": 300.00
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Plate number already exists."));
    }

    @Test
    void shouldRejectCompletedRentalWithoutActualReturnDate() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "Eva",
                "13500000000",
                "eva@example.com",
                "LIC005"
        ));
        Car car = carRepository.save(new Car(
                "H12345",
                "Volkswagen",
                "Golf",
                "White",
                2023,
                CarStatus.AVAILABLE,
                new BigDecimal("190.00")
        ));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "carId": %d,
                                  "rentalDate": "2026-06-10",
                                  "returnDate": "2026-06-12",
                                  "actualReturnDate": null,
                                  "totalAmount": 380.00,
                                  "status": "COMPLETED"
                                }
                                """.formatted(customer.getCustomerId(), car.getCarId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Completed rental must include an actual return date."));
    }

    @Test
    void shouldRejectRentalForUnavailableCar() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "Bob",
                "13900000000",
                "bob@example.com",
                "LIC002"
        ));
        Car car = carRepository.save(new Car(
                "E12345",
                "Audi",
                "A4",
                "Blue",
                2022,
                CarStatus.MAINTENANCE,
                new BigDecimal("260.00")
        ));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "carId": %d,
                                  "rentalDate": "2026-06-10",
                                  "returnDate": "2026-06-12",
                                  "actualReturnDate": null,
                                  "totalAmount": 520.00,
                                  "status": "PENDING_PAYMENT"
                                }
                                """.formatted(customer.getCustomerId(), car.getCarId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Car is not available for rental."));
    }

    @Test
    void shouldRejectDeletingCustomerWithRentalHistory() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "Carol",
                "13700000000",
                "carol@example.com",
                "LIC003"
        ));
        Car car = carRepository.save(new Car(
                "F12345",
                "Nissan",
                "Sylphy",
                "Silver",
                2023,
                CarStatus.AVAILABLE,
                new BigDecimal("120.00")
        ));
        rentalRepository.save(new Rental(
                customer,
                car,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 12),
                null,
                new BigDecimal("240.00"),
                RentalStatus.PENDING_PAYMENT
        ));

        mockMvc.perform(delete("/api/customers/" + customer.getCustomerId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Customer is referenced by rental records and cannot be deleted."));
    }

    @Test
    void shouldRejectPaymentThatExceedsRentalTotal() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "David",
                "13600000000",
                "david@example.com",
                "LIC004"
        ));
        Car car = carRepository.save(new Car(
                "G12345",
                "Mazda",
                "3",
                "Red",
                2024,
                CarStatus.AVAILABLE,
                new BigDecimal("210.00")
        ));
        Rental rental = rentalRepository.save(new Rental(
                customer,
                car,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 12),
                null,
                new BigDecimal("300.00"),
                RentalStatus.PENDING_PAYMENT
        ));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rentalId": %d,
                                  "paymentDate": "2026-06-10",
                                  "amount": 250.00,
                                  "paymentMethod": "WECHAT"
                                }
                                """.formatted(rental.getRentalId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rentalId": %d,
                                  "paymentDate": "2026-06-11",
                                  "amount": 100.00,
                                  "paymentMethod": "ALIPAY"
                                }
                                """.formatted(rental.getRentalId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Payment exceeds the rental total amount."));
    }

    @Test
    void shouldRejectCompletedRentalThatOverlapsExistingRentalPeriod() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "Henry",
                "13200000000",
                "henry@example.com",
                "LIC008"
        ));
        Car car = carRepository.save(new Car(
                "M12345",
                "Toyota",
                "Camry",
                "Gray",
                2023,
                CarStatus.RENTED,
                new BigDecimal("220.00")
        ));
        rentalRepository.save(new Rental(
                customer,
                car,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 15),
                null,
                new BigDecimal("1100.00"),
                RentalStatus.RENTING
        ));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "carId": %d,
                                  "rentalDate": "2026-06-12",
                                  "returnDate": "2026-06-13",
                                  "actualReturnDate": "2026-06-13",
                                  "totalAmount": 440.00,
                                  "status": "COMPLETED"
                                }
                                """.formatted(customer.getCustomerId(), car.getCarId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Car already has an overlapping rental during this period."));
    }

    @Test
    void shouldKeepTargetCarRentedWhenMovingCompletedRentalToCarWithActiveRental() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "Frank",
                "13400000000",
                "frank@example.com",
                "LIC006"
        ));
        Car historyCar = carRepository.save(new Car(
                "J12345",
                "Hyundai",
                "Elantra",
                "Silver",
                2022,
                CarStatus.AVAILABLE,
                new BigDecimal("160.00")
        ));
        Car activeCar = carRepository.save(new Car(
                "K12345",
                "Ford",
                "Focus",
                "Black",
                2023,
                CarStatus.RENTED,
                new BigDecimal("170.00")
        ));
        rentalRepository.save(new Rental(
                customer,
                activeCar,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 12),
                null,
                new BigDecimal("340.00"),
                RentalStatus.RENTING
        ));
        Rental completedRental = rentalRepository.save(new Rental(
                customer,
                historyCar,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 3),
                LocalDate.of(2026, 5, 3),
                new BigDecimal("320.00"),
                RentalStatus.COMPLETED
        ));

        mockMvc.perform(put("/api/rentals/" + completedRental.getRentalId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "carId": %d,
                                  "rentalDate": "2026-05-01",
                                  "returnDate": "2026-05-03",
                                  "actualReturnDate": "2026-05-03",
                                  "totalAmount": 320.00,
                                  "status": "COMPLETED"
                                }
                                """.formatted(customer.getCustomerId(), activeCar.getCarId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId").value(activeCar.getCarId()));

        Car refreshedActiveCar = carRepository.findById(activeCar.getCarId()).orElseThrow();
        assertEquals(CarStatus.RENTED, refreshedActiveCar.getStatus());
    }

    @Test
    void shouldRefreshRentalStatusWhenPaymentIsUpdated() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "Grace",
                "13300000000",
                "grace@example.com",
                "LIC007"
        ));
        Car car = carRepository.save(new Car(
                "L12345",
                "BMW",
                "320i",
                "Blue",
                2024,
                CarStatus.RENTED,
                new BigDecimal("280.00")
        ));
        Rental rental = rentalRepository.save(new Rental(
                customer,
                car,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 12),
                null,
                new BigDecimal("300.00"),
                RentalStatus.PENDING_PAYMENT
        ));

        String paymentResponse = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rentalId": %d,
                                  "paymentDate": "2026-06-10",
                                  "amount": 300.00,
                                  "paymentMethod": "WECHAT"
                                }
                                """.formatted(rental.getRentalId())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paymentId = paymentResponse.replaceAll(".*\"paymentId\":(\\d+).*", "$1");

        mockMvc.perform(put("/api/payments/" + paymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rentalId": %d,
                                  "paymentDate": "2026-06-10",
                                  "amount": 100.00,
                                  "paymentMethod": "WECHAT"
                                }
                                """.formatted(rental.getRentalId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/rentals/" + rental.getRentalId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
    }

    @Test
    void shouldRejectCreatingPaymentForCompletedRental() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "Ivy",
                "13100000004",
                "ivy@example.com",
                "LIC009"
        ));
        Car car = carRepository.save(new Car(
                "N12345",
                "Tesla",
                "Model Y",
                "White",
                2024,
                CarStatus.AVAILABLE,
                new BigDecimal("300.00")
        ));
        Rental rental = rentalRepository.save(new Rental(
                customer,
                car,
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 12),
                LocalDate.of(2026, 6, 12),
                new BigDecimal("600.00"),
                RentalStatus.COMPLETED
        ));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rentalId": %d,
                                  "paymentDate": "2026-06-10",
                                  "amount": 300.00,
                                  "paymentMethod": "WECHAT"
                                }
                                """.formatted(rental.getRentalId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Completed rental payments cannot be modified."));
    }

    @Test
    void shouldRejectDeletingPaymentForCompletedRental() throws Exception {
        Customer customer = customerRepository.save(new Customer(
                "Jack",
                "13100000005",
                "jack@example.com",
                "LIC010"
        ));
        Car car = carRepository.save(new Car(
                "P12345",
                "Toyota",
                "Crown",
                "Black",
                2023,
                CarStatus.AVAILABLE,
                new BigDecimal("320.00")
        ));
        Rental rental = rentalRepository.save(new Rental(
                customer,
                car,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                LocalDate.of(2026, 6, 3),
                new BigDecimal("640.00"),
                RentalStatus.COMPLETED
        ));
        var payment = paymentRepository.save(new com.example.demo.entity.Payment(
                rental,
                LocalDate.of(2026, 6, 1),
                new BigDecimal("640.00"),
                com.example.demo.entity.PaymentMethod.WECHAT
        ));

        mockMvc.perform(delete("/api/payments/" + payment.getPaymentId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Completed rental payments cannot be modified."));
    }
}
