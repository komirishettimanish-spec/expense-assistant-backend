package com.expenseassistant.dto;

public record AuthResponse(String token, UserResponse user) {
}
