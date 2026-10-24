
package com.lawenforcement.controller;

import com.lawenforcement.entity.CaseRecord;
import com.lawenforcement.entity.User;
import com.lawenforcement.repository.UserRepository;
import com.lawenforcement.service.CaseService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cases")
public class CaseController {

    private final CaseService caseService;
    private final UserRepository userRepository;

    public CaseController(
            CaseService caseService,
            UserRepository userRepository) {
        this.caseService = caseService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Login required");
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new AccessDeniedException("User account not found"));
    }

    private void requireAdmin(User user) {
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new AccessDeniedException(
                    "Only administrators can assign cases");
        }
    }

    @GetMapping("/assign")
    public String showAssignmentPage(
            Authentication authentication,
            Model model) {

        User admin = getCurrentUser(authentication);
        requireAdmin(admin);

        List<User> officers =
                userRepository.findByRoleAndAccountStatusOrderByCreatedAtDesc(
                        "INVESTIGATOR OFFICER", "ACTIVE");

        officers.addAll(
                userRepository.findByRoleAndAccountStatusOrderByCreatedAtDesc(
                        "OFFICER", "ACTIVE"));
        model.addAttribute("officers", officers);
        model.addAttribute("cases", caseService.getAllCases());
        model.addAttribute("caseRecord", new CaseRecord());

        return "case-assignment";
    }

    @PostMapping("/assign")
    public String createAndAssignCase(
            @ModelAttribute("caseRecord") CaseRecord caseRecord,
            @RequestParam("officerId") Long officerId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User admin = getCurrentUser(authentication);
        requireAdmin(admin);

        try {
            caseService.createAndAssignCase(
                    caseRecord, officerId, admin);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Case created and assigned successfully.");

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", ex.getMessage());

            return "redirect:/cases/assign";
        }

        return "redirect:/cases/assign";
    }

    @GetMapping("/my-cases")
    public String showMyCases(
            Authentication authentication,
            Model model) {

        User officer = getCurrentUser(authentication);

        if (!"INVESTIGATOR OFFICER".equalsIgnoreCase(officer.getRole())
                && !"OFFICER".equalsIgnoreCase(officer.getRole())) {
            throw new AccessDeniedException(
                    "This page is for investigator officers only");
        }

        model.addAttribute(
                "cases",
                caseService.getCasesAssignedToOfficer(officer.getId()));

        return "case-management";
    }
}

