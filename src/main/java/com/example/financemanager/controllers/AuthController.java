package com.example.financemanager.controllers;

import com.example.financemanager.services.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(Principal principal) {
        // If already logged in, redirect to dashboard
        if (principal != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(Principal principal) {
        // If already logged in, redirect to dashboard
        if (principal != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam @NotBlank(message = "Username is required")
                               @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
                               String username,
                               @RequestParam @NotBlank(message = "Email is required")
                               @Email(message = "Please provide a valid email address")
                               String email,
                               @RequestParam @NotBlank(message = "Password is required")
                               @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?*])(?=\\S+$).{8,}$",
                                       message = "Password must be at least 8 characters and contain uppercase, lowercase, and a symbol")
                               String password) {
        userService.createUser(username, email, password);
        return "redirect:/login";
    }
}