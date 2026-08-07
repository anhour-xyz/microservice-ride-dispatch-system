package com.rideshare.notification_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationEmailRequest(
    @NotBlank
    @Email
    String recipient,

    @NotBlank
    String verificationUrl
) {
}
