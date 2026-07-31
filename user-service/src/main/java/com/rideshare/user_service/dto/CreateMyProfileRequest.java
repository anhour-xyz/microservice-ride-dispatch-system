package com.rideshare.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMyProfileRequest(
        @NotBlank @Size(max = 100) String displayName,
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$") String phoneNumber
) {}
