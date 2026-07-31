package com.rideshare.notification_service.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;
import com.rideshare.notification_service.service.NotificationStreamService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {
    
    private final NotificationStreamService streamService;
    @GetMapping(value = "/stream/{userId}",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private SseEmitter stream(@PathVariable String userId){
        return streamService.connect(userId);
    }
}
