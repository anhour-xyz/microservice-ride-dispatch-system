package com.rideshare.notification_service.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
@Service
public class EmailNotificationService {
    
    private final JavaMailSender javaMailSender;
    private final String fromAddress;

    public EmailNotificationService(JavaMailSender javaMailSender, @Value("${app.mail.from}") String fromAddress) {
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationEmail(String recipient, String verificationUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject("Account Verification");
        message.setText("Welcome to the Relay Ride! Please click the link below to verify your account: " + verificationUrl);
        javaMailSender.send(message);
    }
}
