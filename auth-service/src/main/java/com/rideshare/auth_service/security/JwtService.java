package com.rideshare.auth_service.security;

import com.rideshare.auth_service.entity.Credential;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {
    private static final Duration LIFETIME = Duration.ofMinutes(15);
    private final JwtEncoder encoder;
    public JwtService(JwtEncoder encoder) { this.encoder = encoder; }

    public String generateAccessToken(Credential credential) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ride-auth-service")
                .issuedAt(now).expiresAt(now.plus(LIFETIME))
                .subject(credential.getUserId().toString())
                .claim("email", credential.getEmail())
                .claim("roles", credential.getRoles().stream().map(Enum::name).sorted().toList())
                .build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
