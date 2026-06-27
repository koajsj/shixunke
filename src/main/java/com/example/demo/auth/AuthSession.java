package com.example.demo.auth;

public record AuthSession(String username, AppRole role, Long customerId) {
}
