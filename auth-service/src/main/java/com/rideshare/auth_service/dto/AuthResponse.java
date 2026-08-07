package com.rideshare.auth_service.dto;

import java.util.UUID;
public record AuthResponse(UUID userId, String accessToken, String refreshToken, String tokenType, long expiresIn) {}
