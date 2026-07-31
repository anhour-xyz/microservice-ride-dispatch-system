package com.rideshare.user_service.controller;

import com.rideshare.user_service.dto.CreateMyProfileRequest;
import com.rideshare.user_service.dto.CreateUserRequest;
import com.rideshare.user_service.dto.UpdateUserRequest;
import com.rideshare.user_service.dto.UserResponse;
import com.rideshare.user_service.entity.UserRole;
import com.rideshare.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/me")
    public ResponseEntity<UserResponse> createCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateMyProfileRequest request
    ) {
        CreateUserRequest createRequest = new CreateUserRequest(
                extractUserId(jwt),
                request.displayName(),
                request.phoneNumber(),
                extractRoles(jwt)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(createRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUser(extractUserId(jwt)));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(extractUserId(jwt), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        userService.deactivateUser(extractUserId(jwt));
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private Set<UserRole> extractRoles(Jwt jwt) {
        return jwt.getClaimAsStringList("roles").stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toUnmodifiableSet());
    }
}
