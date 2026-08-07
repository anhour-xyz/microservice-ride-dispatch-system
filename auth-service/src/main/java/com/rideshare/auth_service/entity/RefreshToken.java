package com.rideshare.auth_service.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @UuidGenerator private UUID id;
    @Column(name = "user_id", nullable = false, updatable = false) private UUID userId;
    @Column(name = "token_hash", nullable = false, unique = true, updatable = false) private String tokenHash;
    @Column(name = "revoked_at") private Instant revokedAt;
    @Column(name = "expires_at", nullable = false, updatable = false) private Instant expiresAt;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected RefreshToken() {}
    public RefreshToken(UUID userId, String tokenHash, Instant expiresAt) {
        this.userId = userId; this.tokenHash = tokenHash; this.expiresAt = expiresAt;
    }
    public UUID getUserId() { return userId; }
    public boolean isRevoked() { return revokedAt != null; }
    public boolean isExpired(Instant now) { return !expiresAt.isAfter(now); }
    public void revoke() { if (revokedAt == null) revokedAt = Instant.now(); }
}
