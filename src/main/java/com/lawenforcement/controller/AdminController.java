package com.lawenforcement.controller;

import com.lawenforcement.entity.User;
import com.lawenforcement.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/registrations")
    public String pendingRegistrations(Model model) {

        model.addAttribute(
                "pendingUsers",
                userRepository.findByAccountStatusOrderByCreatedAtDesc(
                        "PENDING_APPROVAL"
                )
        );

        return "admin-registrations";
    }

    @PostMapping("/admin/approve/{id}")
    public String approveUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "User not found."
            );
            return "redirect:/admin/registrations";
        }

        if ("ADMIN".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator accounts cannot be approved here."
            );
            return "redirect:/admin/registrations";
        }

        user.setAccountStatus("ACTIVE");

        // Do not claim email verification has occurred.
        // A real verification workflow must be implemented separately.

        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Account approved. Email verification is still required before login."
        );

        return "redirect:/admin/registrations";
    }

    @PostMapping("/admin/reject/{id}")
    public String rejectUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "User not found."
            );
            return "redirect:/admin/registrations";
        }

        if ("ADMIN".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator accounts cannot be rejected here."
            );
            return "redirect:/admin/registrations";
        }

        user.setAccountStatus("REJECTED");
        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Registration rejected."
        );

        return "redirect:/admin/registrations";
    }

    @PostMapping("/admin/users/{id}/suspend")
    public String suspendUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "User not found."
            );
        } else if ("ADMIN".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator accounts cannot be suspended here."
            );
        } else {
            user.setAccountStatus("SUSPENDED");
            userRepository.save(user);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Account suspended."
            );
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/reactivate")
    public String reactivateUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "User not found."
            );
        } else if ("ADMIN".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Administrator accounts cannot be reactivated here."
            );
        } else {
            user.setAccountStatus("ACTIVE");
            userRepository.save(user);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Account reactivated. Email verification is still required if it has not been completed."
            );
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users")
    public String manageUsers(Model model) {

        model.addAttribute(
                "users",
                userRepository.findAllByOrderByCreatedAtDesc()
        );

        return "admin-users";
    }

    @PostMapping("/admin/users/{id}/role")
    public String changeRole(
            @PathVariable Long id,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "User not found."
            );
            return "redirect:/admin/users";
        }

        if ("ADMIN".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This page cannot change administrator roles."
            );
            return "redirect:/admin/users";
        }

        if (!"OFFICER".equals(role)
                && !"INVESTIGATOR".equals(role)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Invalid role."
            );
            return "redirect:/admin/users";
        }

        user.setRole(role);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "successMessage", "User role updated."
        );

        return "redirect:/admin/users";
    }
}