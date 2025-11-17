package com.project.cinemabackend.controller;

import com.project.cinemabackend.dto.auth.LoginRequest;
import com.project.cinemabackend.dto.auth.LoginResponse;
import com.project.cinemabackend.dto.auth.RegisterRequest;
import com.project.cinemabackend.dto.auth.RegisterResponse;
import com.project.cinemabackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/public/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/public/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        return authService.register(request, response);
    }

    @PostMapping("/public/auth/refresh")
    public ResponseEntity<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        return authService.refresh(request, response);
    }

    @PostMapping("/public/auth/logout")
    public ResponseEntity<LoginResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        return authService.logout(request, response);
    }
}