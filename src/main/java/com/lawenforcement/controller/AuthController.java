package com.lawenforcement.controller;

import com.lawenforcement.dto.RegistrationForm;
import com.lawenforcement.service.RegistrationService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute(
                "registrationForm",
                new RegistrationForm()
        );

        return "register";
    }

    @PostMapping("/register")
    public String submitRegistration(
            @Valid @ModelAttribute("registrationForm")
            RegistrationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "error",
                    "Please check all fields and password requirements."
            );
            return "register";
        }

        try {
            registrationService.register(form);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Registration submitted. Await administrator approval and email verification."
            );

            return "redirect:/login?registered";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("error", ex.getMessage());

            return "register";
        }
    }
}