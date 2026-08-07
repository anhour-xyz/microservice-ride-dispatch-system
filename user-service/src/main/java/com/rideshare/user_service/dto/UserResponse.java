package com.rideshare.user_service.dto;
import com.rideshare.user_service.entity.UserRole;
import com.rideshare.user_service.entity.UserStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
public record UserResponse(UUID userId, String displayName, String phoneNumber, Set<UserRole> roles, UserStatus status, Instant createdAt, Instant updatedAt) {}
