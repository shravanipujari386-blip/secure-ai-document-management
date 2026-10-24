package com.lawenforcement.config;

import com.lawenforcement.entity.User;
import com.lawenforcement.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    private static final String ADMIN_EMAIL = "admin@demo.gov.in";
    private static final String ADMIN_PASSWORD = "AdminDemo@2026Secure";

    @Bean
    CommandLineRunner createInitialAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)) {
                System.out.println("Demo administrator already exists.");
                return;
            }

            User admin = new User();

            admin.setFullName("System Administrator");
            admin.setEmail(ADMIN_EMAIL);
            admin.setPasswordHash(
                    passwordEncoder.encode(ADMIN_PASSWORD)
            );
            admin.setDesignation("System Administrator");
            admin.setDepartment("Administration");
            admin.setRole("ADMIN");
            admin.setAccountStatus("ACTIVE");
            admin.setEmailVerified(true);

            userRepository.save(admin);

            System.out.println(
                    "Demo administrator account created: " + ADMIN_EMAIL
            );
        };
    }
}