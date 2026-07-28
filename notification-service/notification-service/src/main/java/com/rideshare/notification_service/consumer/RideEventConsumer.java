package com.rideshare.notification_service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.rideshare.notification_service.event.RideMatchedEvent;
import com.rideshare.notification_service.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RideEventConsumer {
    
    private final NotificationService notificationService;

    @KafkaListener( topics = "ride.matched", groupId = "notification-service-group")
    public void consumeRideMatched(RideMatchedEvent event){
        notificationService.notifyRideMatched(event);
    }
}
