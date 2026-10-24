
package com.lawenforcement.config;

import com.lawenforcement.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // Redirect users according to their role after login
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {

        return (request, response, authentication) -> {

            boolean isAdmin = authentication.getAuthorities()
                    .stream()
                    .anyMatch(authority ->
                            authority.getAuthority()
                                    .equals("ROLE_ADMIN"));

            if (isAdmin) {
                response.sendRedirect(
                        request.getContextPath()
                                + "/admin/registrations");
            } else {
                response.sendRedirect(
                        request.getContextPath() + "/dashboard");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            AuthenticationSuccessHandler authenticationSuccessHandler)
            throws Exception {

        http.authenticationProvider(authenticationProvider);

        http.authorizeHttpRequests(auth -> auth

                // Public pages
                .requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/verify-email",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error"
                ).permitAll()

                // Administrator-only document management
                // Includes admin upload, download, and delete routes
                .requestMatchers("/admin/documents/**")
                .hasRole("ADMIN")
                
                
             // Only administrators can download documents
             .requestMatchers("/documents/*/download")
             .hasRole("ADMIN")
             


             
          // Shared document routes for authorized roles
          .requestMatchers(
                  "/documents",
                  "/documents/",
                  "/documents/upload",
                  "/documents/*/view",
                  "/documents/*/edit",
                  "/documents/*/update",
                  "/documents/*/analysis"
          ).hasAnyRole(
                  "ADMIN",
                  "INVESTIGATOR_OFFICER",
                  "AUTHORIZED_USER"
          )
          


                // All other administrator pages remain Admin-only
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                // All remaining pages require authentication
                .anyRequest().authenticated()
        );

        // Login configuration
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error")
                .permitAll()
        );

        // Logout configuration
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
        );

        // Keep CSRF protection enabled
        return http.build();
    }
}

