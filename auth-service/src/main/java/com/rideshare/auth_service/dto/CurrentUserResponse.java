package com.rideshare.auth_service.dto;

import com.rideshare.auth_service.entity.Role;
import java.util.Set;
import java.util.UUID;
public record CurrentUserResponse(UUID userId, String email, Set<Role> roles) {}
