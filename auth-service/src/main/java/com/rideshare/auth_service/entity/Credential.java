package com.rideshare.auth_service.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.ElementCollection;
import java.time.Instant;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;


@Entity
@Table(
    name = "credentials",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_credentials_email",
            columnNames = "email"
        )
    }
)
public class Credential {
    
    @Id
    @UuidGenerator
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "credential_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();

    @Column(nullable= false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    protected Credential(){}

    public Credential(String email, String passwordHash, Set<Role> roles){
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
    }

    public UUID getUserId(){return userId;}
    public String getEmail(){return email;}
    public String getPasswordHash() {return passwordHash;}
    public Set<Role> getRoles() {return Set.copyOf(roles);}
    public boolean isEnabled(){return enabled;}
    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public void disable(){this.enabled = false;}
    public void enable(){this.enabled = true;}

    public boolean isEmailVerified() {
    return emailVerified;
}
    public void verifyEmail() {
    this.emailVerified = true;
}
}

