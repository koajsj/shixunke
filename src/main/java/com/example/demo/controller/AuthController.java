package com.example.demo.controller;

import com.example.demo.auth.AuthService;
import com.example.demo.auth.AuthSession;
import com.example.demo.auth.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthSession authSession = authService.authenticate(request.getUsername(), request.getPassword());
        HttpSession session = httpRequest.getSession();
        httpRequest.changeSessionId();
        session.setAttribute(AuthService.SESSION_KEY, authSession);
        return toResponse(authSession);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        AuthSession authSession = session == null ? null : (AuthSession) session.getAttribute(AuthService.SESSION_KEY);
        if (authSession == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(toResponse(authSession));
    }

    private Map<String, Object> toResponse(AuthSession authSession) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("username", authSession.username());
        response.put("role", authSession.role().name());
        response.put("customerId", authSession.customerId());
        return response;
    }
}
