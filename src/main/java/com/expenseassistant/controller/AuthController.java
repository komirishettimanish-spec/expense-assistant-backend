package com.expenseassistant.controller;

import com.expenseassistant.dto.AuthResponse;
import com.expenseassistant.dto.LoginRequest;
import com.expenseassistant.dto.RegisterRequest;
import com.expenseassistant.dto.UserResponse;
import com.expenseassistant.service.AuthService;
import com.expenseassistant.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(currentUserService.getCurrentUser());
    }
}
