package com.rideshare.auth_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;

@Component
public class HTTPNotificationClient
        implements NotificationClient {

        private final RestClient restClient;
        public HTTPNotificationClient(RestClient.Builder builder,
            @Value("${app.notification-service.base-url}") String baseUrl) {
                this.restClient = builder.baseUrl(baseUrl).build();
        }

        @Override
        public void sendVerificationEmail(String recipient, String verificationUrl){
                VerificationEmailRequest request = new VerificationEmailRequest(recipient, verificationUrl);
                restClient.post()
                        .uri("/api/v1/internal/email/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();
        }

        private record VerificationEmailRequest(String recipient, String verificationUrl) {
        }
}
