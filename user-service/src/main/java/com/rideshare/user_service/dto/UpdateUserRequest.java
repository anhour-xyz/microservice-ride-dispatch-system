package com.rideshare.user_service.dto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record UpdateUserRequest(@Size(min=1,max=100) String displayName, @Pattern(regexp="^\\+?[1-9]\\d{7,14}$") String phoneNumber) {}
