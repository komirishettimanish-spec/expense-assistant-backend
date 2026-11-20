package com.expenseassistant.dto;

import com.expenseassistant.entity.User;

public record UserResponse(Long id, String name, String username, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getUsername(), user.getEmail());
    }
}
