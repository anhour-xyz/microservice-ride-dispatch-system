package com.rideshare.auth_service.dto;

import com.rideshare.auth_service.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message ="Password is required")
    @Size(
        min =8,
        max =72,
        message ="Password must contain between 8-72 characters"
    ) 
    String password,

    @NotNull(message = "Role is required")
    Role role
) {
    
}
