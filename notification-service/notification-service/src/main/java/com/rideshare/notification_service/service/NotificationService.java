package com.rideshare.notification_service.service;

import org.springframework.stereotype.Service;

import com.rideshare.notification_service.dto.NotificationResponse;
import com.rideshare.notification_service.event.RideMatchedEvent;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationStreamService streamService;

    public void notifyRideMatched(RideMatchedEvent event){
        NotificationResponse riderNotification = new NotificationResponse(
            event.getRiderId(),
            "RIDE_MATCHED",
            "Driver found",
            "Driver  " + event.getDriverId() + " was assigned to your ride.",
            event.getRideId(),
            Instant.now()
        );

        NotificationResponse driverNotification = new NotificationResponse(
            event.getDriverId(),
            "RIDE_ASSIGNED",
            "New ride assigned",
            "You were assigned to ride " + event.getRideId() + ".",
            event.getRideId(),
            Instant.now()
        );

        streamService.send(riderNotification);
        streamService.send(driverNotification);
    }
}