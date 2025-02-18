package com.tongji.wordtrail.controller;

import com.tongji.wordtrail.dto.AuthResponse;
import com.tongji.wordtrail.dto.LoginRequest;
import com.tongji.wordtrail.dto.RegisterRequest;
import com.tongji.wordtrail.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
        logger.info("AuthController initialized");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        logger.info("Received registration request for username: {}", request.getUsername());
        try {
            AuthResponse response = authService.register(request);
            logger.info("Successfully registered user. Username: {}, UserId: {}",
                    response.getUsername(), response.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Registration failed for username: {}. Error: {}",
                    request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        logger.info("Received login request for username: {}", request.getUsername());
        try {
            AuthResponse response = authService.login(request);
            logger.info("Successfully logged in user. Username: {}, UserId: {}",
                    response.getUsername(), response.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for username: {}. Error: {}",
                    request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
}