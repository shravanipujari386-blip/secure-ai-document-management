package com.lawenforcement.service;

import com.lawenforcement.entity.User;
import com.lawenforcement.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {


private final UserRepository userRepository;

private static final Set<String> ALLOWED_ROLES = Set.of(
        "ADMIN",
        "INVESTIGATOR_OFFICER",
        "AUTHORIZED_USER"
);

public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
}

@Override
public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException {

    String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

    User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() ->
                    new UsernameNotFoundException(
                            "Invalid email or password"
                    )
            );

    String role = user.getRole() == null
            ? ""
            : user.getRole().trim()
                    .toUpperCase(Locale.ROOT)
                    .replace(' ', '_');

    if (!ALLOWED_ROLES.contains(role)) {
        throw new UsernameNotFoundException(
                "Invalid email or password"
        );
    }

    boolean active =
            "ACTIVE".equalsIgnoreCase(user.getAccountStatus());

    boolean verified = user.isEmailVerified();

    return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPasswordHash())
            .roles(role)
            .disabled(!active || !verified)
            .build();
}


}
