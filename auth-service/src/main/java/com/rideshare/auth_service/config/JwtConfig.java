package com.rideshare.auth_service.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Value("${jwt.public-key}")
    private Resource publicKeyResource;

    @Value("${jwt.private-key}")
    private Resource privateKeyResource;

    @Bean
    public RSAKey rsaKey() {
        try (
            InputStream publicKeyStream =
                    publicKeyResource.getInputStream();

            InputStream privateKeyStream =
                    privateKeyResource.getInputStream()
        ) {
            RSAPublicKey publicKey =
                    RsaKeyConverters.x509()
                            .convert(publicKeyStream);

            RSAPrivateKey privateKey =
                    RsaKeyConverters.pkcs8()
                            .convert(privateKeyStream);

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("auth-service-key")
                    .build();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load RSA key files",
                    exception
            );
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        JWKSource<SecurityContext> source =
                new ImmutableJWKSet<>(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(source);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey)
            throws JOSEException {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(rsaKey.toRSAPublicKey())
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        "ride-auth-service"
                )
        );

        return decoder;
    }
}