package com.rideshare.auth_service.repository;

import com.rideshare.auth_service.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    void deleteByUserIdAndUsedAtIsNull(UUID userId);
}