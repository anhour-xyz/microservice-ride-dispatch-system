package com.rideshare.auth_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class HTTPNotificationClient
        implements NotificationClient {

    private static final Logger log =
            LoggerFactory.getLogger(HTTPNotificationClient.class);

    @Override
    public void sendVerificationEmail(
            String recipient,
            String verificationUrl
    ) {
        log.info(
                "Email verification requested: recipient={}, url={}",
                recipient,
                verificationUrl
        );
    }
}