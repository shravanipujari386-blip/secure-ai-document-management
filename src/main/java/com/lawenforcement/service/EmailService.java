
package com.lawenforcement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(
            String recipientEmail,
            String token) {

        String verificationLink =
                baseUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("Verify Your Email Address");

        message.setText(
                "Hello,\n\n"
                + "Thank you for registering with the "
                + "Law Enforcement Digital Document Management System.\n\n"
                + "Please click the link below to verify your email:\n\n"
                + verificationLink
                + "\n\nThis link expires in 30 minutes.\n\n"
                + "If you did not register, please ignore this email."
        );

        mailSender.send(message);
    }
}

