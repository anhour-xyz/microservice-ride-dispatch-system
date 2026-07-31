package com.rideshare.auth_service.service;
import com.rideshare.auth_service.dto.RegisterResponse;
import com.rideshare.auth_service.exception.EmailNotVerifiedException;
import com.rideshare.auth_service.dto.AuthResponse;
import com.rideshare.auth_service.dto.LoginRequest;
import com.rideshare.auth_service.entity.Credential;
import com.rideshare.auth_service.entity.RefreshToken;
import com.rideshare.auth_service.exception.EmailAlreadyExistsException;
import com.rideshare.auth_service.exception.InvalidCredentialsException;
import com.rideshare.auth_service.repository.CredentialRepository;
import com.rideshare.auth_service.repository.RefreshTokenRepository;
import com.rideshare.auth_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rideshare.auth_service.dto.RegisterRequest;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(30);

    private final CredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;

    public AuthService(CredentialRepository credentialRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                    EmailVerificationService emailVerificationService) {
        this.credentialRepository = credentialRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.emailVerificationService =emailVerificationService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (credentialRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        Credential credential = new Credential(
                email,
                passwordEncoder.encode(request.password()),
                Set.of(request.role())
        );

        Credential savedCredential = credentialRepository.save(credential);
        emailVerificationService.sendVerification(savedCredential);
        return new RegisterResponse(
            savedCredential.getUserId(),
            "Registration successful. Check your email."
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Credential credential = credentialRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!credential.isEnabled()
                || !passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if(!credential.isEmailVerified()){
            throw new EmailNotVerifiedException();
        }
        return issueTokens(credential);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken storedToken = refreshTokenService.validate(rawRefreshToken);
        Credential credential = credentialRepository.findById(storedToken.getUserId())
                .orElseThrow(InvalidCredentialsException::new);
        if (!credential.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        if(!credential.isEmailVerified()){
            throw new EmailNotVerifiedException();
        }

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
        return issueTokens(credential);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private AuthResponse issueTokens(Credential credential) {
        return new AuthResponse(
                credential.getUserId(),
                jwtService.generateAccessToken(credential),
                refreshTokenService.create(credential.getUserId(), REFRESH_TOKEN_LIFETIME),
                "Bearer",
                ACCESS_TOKEN_LIFETIME.toSeconds()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}


