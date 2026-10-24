
package com.lawenforcement.controller;

import com.lawenforcement.service.EmailVerificationService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmailVerificationController {

    private final EmailVerificationService verificationService;

    public EmailVerificationController(
            EmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam(name = "token", required = false)
            String token,
            Model model) {

        String message = verificationService.verifyEmail(token);

        model.addAttribute("message", message);

        return "verify-email";
    }
}


