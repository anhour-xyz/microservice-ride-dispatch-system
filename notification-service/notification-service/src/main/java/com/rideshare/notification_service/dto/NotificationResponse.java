package com.rideshare.notification_service.dto;

import java.time.Instant;
public record NotificationResponse(
    String recipientId,
    String type,
    String title,
    String message,
    String rideId,
    Instant createAt) {
}
