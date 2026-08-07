package com.rideshare.notification_service.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideshare.notification_service.dto.VerificationEmailRequest;
import com.rideshare.notification_service.service.EmailNotificationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/internal/email")

public class EmailNotificationController{
    private final EmailNotificationService emailNotificationService;
    
    public EmailNotificationController(EmailNotificationService emailNotificationService){
        this.emailNotificationService = emailNotificationService;
    }

    @PostMapping("/verification")
    public ResponseEntity<Void> sendVerificationEmail(@Valid @RequestBody VerificationEmailRequest request){
        emailNotificationService.sendVerificationEmail(request.recipient(), request.verificationUrl());
        return ResponseEntity.accepted().build();
    }
}