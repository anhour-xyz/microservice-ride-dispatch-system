package com.rideshare.auth_service.service;

import com.rideshare.auth_service.entity.RefreshToken;
import com.rideshare.auth_service.exception.InvalidRefreshTokenException;
import com.rideshare.auth_service.repository.RefreshTokenRepository;
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
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final SecureRandom random = new SecureRandom();
    public RefreshTokenService(RefreshTokenRepository repository) { this.repository = repository; }

    @Transactional
    public String create(UUID userId, Duration lifetime) {
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new RefreshToken(userId, hash(raw), Instant.now().plus(lifetime)));
        return raw;
    }
    @Transactional(readOnly = true)
    public RefreshToken validate(String raw) {
        RefreshToken token = repository.findByTokenHash(hash(raw)).orElseThrow(InvalidRefreshTokenException::new);
        if (token.isRevoked() || token.isExpired(Instant.now())) throw new InvalidRefreshTokenException();
        return token;
    }
    @Transactional
    public void revoke(String raw) { RefreshToken token = validate(raw); token.revoke(); repository.save(token); }
    private String hash(String value) {
        if (value == null || value.isBlank()) throw new InvalidRefreshTokenException();
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
}
