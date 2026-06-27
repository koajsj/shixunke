package com.example.demo.auth;

import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    public static final String SESSION_KEY = "AUTH_SESSION";

    private final String adminUsername;
    private final String adminPassword;
    private final String userUsername;
    private final String userPassword;
    private final CustomerRepository customerRepository;
    private final String userCustomerName;
    private final String userCustomerPhone;
    private final String userCustomerEmail;
    private final String userCustomerLicenseNumber;

    public AuthService(
            CustomerRepository customerRepository,
            @Value("${app.auth.admin.username:admin}") String adminUsername,
            @Value("${app.auth.admin.password:123}") String adminPassword,
            @Value("${app.auth.user.username:user}") String userUsername,
            @Value("${app.auth.user.password:123}") String userPassword,
            @Value("${app.auth.user.customer-name:普通用户}") String userCustomerName,
            @Value("${app.auth.user.customer-phone:13900009999}") String userCustomerPhone,
            @Value("${app.auth.user.customer-email:user@example.com}") String userCustomerEmail,
            @Value("${app.auth.user.customer-license-number:USER0001}") String userCustomerLicenseNumber
    ) {
        this.customerRepository = customerRepository;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.userUsername = userUsername;
        this.userPassword = userPassword;
        this.userCustomerName = userCustomerName;
        this.userCustomerPhone = userCustomerPhone;
        this.userCustomerEmail = userCustomerEmail;
        this.userCustomerLicenseNumber = userCustomerLicenseNumber;
    }

    public AuthSession authenticate(String username, String password) {
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            return new AuthSession(adminUsername, AppRole.ADMIN, null);
        }
        if (userUsername.equals(username) && userPassword.equals(password)) {
            Customer customer = customerRepository.findByEmail(userCustomerEmail)
                    .map(this::repairDefaultUserCustomer)
                    .orElseGet(this::createDefaultUserCustomer);
            return new AuthSession(userUsername, AppRole.USER, customer.getCustomerId());
        }
        throw new ResponseStatusException(UNAUTHORIZED, "Username or password is incorrect.");
    }

    private Customer repairDefaultUserCustomer(Customer customer) {
        boolean changed = false;
        if (!userCustomerName.equals(customer.getName())) {
            customer.setName(userCustomerName);
            changed = true;
        }
        if (!userCustomerPhone.equals(customer.getPhone())) {
            customer.setPhone(userCustomerPhone);
            changed = true;
        }
        if (!userCustomerLicenseNumber.equals(customer.getLicenseNumber())) {
            customer.setLicenseNumber(userCustomerLicenseNumber);
            changed = true;
        }
        return changed ? customerRepository.save(customer) : customer;
    }

    private Customer createDefaultUserCustomer() {
        Customer customer = new Customer(
                userCustomerName,
                userCustomerPhone,
                userCustomerEmail,
                userCustomerLicenseNumber
        );
        return customerRepository.save(customer);
    }
}
