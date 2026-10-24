
package com.lawenforcement.service;

import com.lawenforcement.entity.EmailVerificationToken;
import com.lawenforcement.entity.User;
import com.lawenforcement.repository.EmailVerificationTokenRepository;
import com.lawenforcement.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            EmailService emailService) {

        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void sendVerificationEmail(String email) {

        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken =
                new EmailVerificationToken(
                        token,
                        normalizedEmail,
                        LocalDateTime.now().plusMinutes(30)
                );

        tokenRepository.saveAndFlush(verificationToken);

        emailService.sendVerificationEmail(
                normalizedEmail,
                token
        );
    }

    @Transactional
    public String verifyEmail(String token) {

        if (token == null || token.isBlank()) {
            return "Invalid verification link.";
        }

        EmailVerificationToken verificationToken =
                tokenRepository.findByToken(token).orElse(null);

        if (verificationToken == null) {
            return "Invalid verification link.";
        }

        if (verificationToken.isUsed()) {
            return "This verification link has already been used.";
        }

        if (!verificationToken.getExpiryDate()
                .isAfter(LocalDateTime.now())) {

            return "This verification link has expired. "
                    + "Please request a new verification email.";
        }

        User user = userRepository
                .findByEmailIgnoreCase(verificationToken.getEmail())
                .orElse(null);

        if (user == null) {
            return "The associated account was not found.";
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        return "Your email has been verified successfully! "
                + "Your account may still require administrator approval.";
    }
}


