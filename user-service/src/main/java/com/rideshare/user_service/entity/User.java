package com.rideshare.user_service.entity;
import java.util.UUID;
import jakarta.persistence.CollectionTable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<UserRole> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected User(){

    }

    public User(
        UUID userId,
        String displayName,
        String phoneNumber,
        Set<UserRole> roles
    ){
        this.userId = userId;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.roles = new HashSet<>(roles);
    }

    public UUID getUserId(){return userId;}
    public String getDisplayName(){return displayName;}
    public String getPhoneNumber(){return phoneNumber;}
    public Set<UserRole> getRoles(){return roles;}
    public UserStatus getStatus(){return status;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getUpdatedAt(){return updatedAt;}

    public void updateProfile(String displayName, String phoneNumber){
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
    }

    public void suspend(){this.status = UserStatus.SUSPENDED;}
    public void activate(){this.status = UserStatus.ACTIVE;}
    public void deactivate(){this.status = UserStatus.DEACTIVATED;}


}
