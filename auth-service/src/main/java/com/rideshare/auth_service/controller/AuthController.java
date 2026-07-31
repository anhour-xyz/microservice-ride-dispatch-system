package com.rideshare.auth_service.controller;

import com.rideshare.auth_service.dto.*;
import com.rideshare.auth_service.entity.Role;
import com.rideshare.auth_service.service.AuthService;
import com.rideshare.auth_service.service.EmailVerificationService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(AuthService authService, EmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(@AuthenticationPrincipal Jwt jwt) {
        List<String> roleClaims = jwt.getClaimAsStringList("roles");
        Set<Role> roles = roleClaims.stream()
                .map(Role::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        return new CurrentUserResponse(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                roles
        );
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
        @Valid @RequestBody VerifyEmailRequest request
    ){
        emailVerificationService.verify(request.token());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(
        @Valid @RequestBody ResendVerificationRequest request
    ){
        emailVerificationService.resend(request.email());
        return ResponseEntity.accepted().build();
    }
    
}
