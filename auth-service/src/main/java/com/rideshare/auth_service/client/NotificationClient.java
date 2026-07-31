package com.rideshare.auth_service.client;

public interface NotificationClient {

    void sendVerificationEmail(
            String recipient,
            String verificationUrl
    );
}