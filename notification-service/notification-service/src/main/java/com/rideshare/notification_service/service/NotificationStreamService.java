package com.rideshare.notification_service.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.rideshare.notification_service.dto.NotificationResponse;

@Service
public class NotificationStreamService {
    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    public SseEmitter connect(String userId){
        SseEmitter emitter = new SseEmitter(0L);
        clients.put(userId, emitter);

        emitter.onCompletion(() -> clients.remove(userId,emitter));
        emitter.onTimeout(() -> clients.remove(userId, emitter));
        emitter.onError(error -> clients.remove(userId, emitter));

        try{
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Notification stream connected"));
        } catch (IOException exception) {
            clients.remove(userId,emitter);
            emitter.completeWithError(exception);
        }

        return emitter;
    }


    public void send(NotificationResponse notification){
        SseEmitter emitter = clients.get(notification.recipientId());
        if (emitter == null){
            return;
        }

        try {
            emitter.send(
                SseEmitter.event()
                .name("notification")
                .data(notification)
            );
        } catch (IOException exception) {
            clients.remove(notification.recipientId(), emitter);
            emitter.completeWithError(exception);
        }
    }
}
