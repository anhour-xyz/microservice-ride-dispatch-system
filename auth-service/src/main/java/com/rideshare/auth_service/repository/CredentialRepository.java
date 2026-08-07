package com.rideshare.auth_service.repository;

import com.rideshare.auth_service.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByEmail(String email);
    boolean existsByEmail(String email);
}
