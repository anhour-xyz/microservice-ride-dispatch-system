package com.rideshare.auth_service.service;

import com.rideshare.auth_service.client.NotificationClient;
import com.rideshare.auth_service.entity.Credential;
import com.rideshare.auth_service.entity.EmailVerificationToken;
import com.rideshare.auth_service.exception.InvalidVerificationTokenException;
import com.rideshare.auth_service.exception.VerificationTokenExpiredException;
import com.rideshare.auth_service.repository.CredentialRepository;
import com.rideshare.auth_service.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final CredentialRepository credentialRepository;
    private final NotificationClient notificationClient;
    private final String frontendBaseUrl;
    private final Duration tokenLifetime;

    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            CredentialRepository credentialRepository,
            NotificationClient notificationClient,
            @Value("${app.frontend.base-url}") String frontendBaseUrl,
            @Value("${app.email-verification.expiration-minutes}")
            long expirationMinutes
    ) {
        this.tokenRepository = tokenRepository;
        this.credentialRepository = credentialRepository;
        this.notificationClient = notificationClient;
        this.frontendBaseUrl = removeTrailingSlash(frontendBaseUrl);
        this.tokenLifetime = Duration.ofMinutes(expirationMinutes);
    }

    @Transactional
    public void sendVerification(Credential credential) {
        if (credential.isEmailVerified()) {
            return;
        }

        tokenRepository.deleteByUserIdAndUsedAtIsNull(
                credential.getUserId()
        );

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token =
                new EmailVerificationToken(
                        credential.getUserId(),
                        tokenHash,
                        Instant.now().plus(tokenLifetime)
                );

        tokenRepository.save(token);

        String verificationUrl = frontendBaseUrl
                + "/verify-email?token="
                + rawToken;

        notificationClient.sendVerificationEmail(
                credential.getEmail(),
                verificationUrl
        );
    }

    @Transactional
    public void verify(String rawToken) {
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken token = tokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(InvalidVerificationTokenException::new);

        if (token.isUsed()) {
            throw new InvalidVerificationTokenException();
        }

        if (token.isExpired(Instant.now())) {
            throw new VerificationTokenExpiredException();
        }

        Credential credential = credentialRepository
                .findById(token.getUserId())
                .orElseThrow(InvalidVerificationTokenException::new);

        credential.verifyEmail();
        token.markUsed();
    }

    @Transactional
    public void resend(String email) {
        credentialRepository
                .findByEmail(normalizeEmail(email))
                .filter(credential -> !credential.isEmailVerified())
                .ifPresent(this::sendVerification);
    }

    private String generateRawToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    private String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidVerificationTokenException();
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String removeTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }
}