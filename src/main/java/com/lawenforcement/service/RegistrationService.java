
package com.lawenforcement.service;

import com.lawenforcement.dto.RegistrationForm;
import com.lawenforcement.entity.User;
import com.lawenforcement.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public void register(RegistrationForm form) {

        String email = form.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        String requestedRole = form.getRequestedRole();

        if (!"INVESTIGATOR OFFICER".equals(requestedRole)
                && !"AUTHORIZED USER".equals(requestedRole)) {

            throw new IllegalArgumentException(
                    "Please select a valid user role."
            );
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {

            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        User user = new User();

        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setDesignation(form.getDesignation().trim());
        user.setDepartment(form.getDepartment().trim());

        user.setPasswordHash(
                passwordEncoder.encode(form.getPassword())
        );

        user.setRole(requestedRole);
        user.setAccountStatus("PENDING_APPROVAL");
        user.setEmailVerified(false);

        userRepository.saveAndFlush(user);

        emailVerificationService.sendVerificationEmail(email);
    }
}

