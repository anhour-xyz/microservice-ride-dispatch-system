package com.rideshare.auth_service.dto;
import java.util.UUID;
public record RegisterResponse(
    UUID userId,
    String message
) {
    
}
