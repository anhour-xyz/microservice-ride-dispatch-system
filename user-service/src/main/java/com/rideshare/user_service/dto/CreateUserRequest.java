package com.rideshare.user_service.dto;
import com.rideshare.user_service.entity.UserRole;
import java.util.Set;
import java.util.UUID;
public record CreateUserRequest(UUID userId, String displayName, String phoneNumber, Set<UserRole> roles) {}
