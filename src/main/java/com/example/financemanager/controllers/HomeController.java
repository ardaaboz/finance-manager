package com.example.financemanager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Principal principal) {
        // If user is logged in, go to dashboard
        if (principal != null) {
            return "redirect:/dashboard";
        }
        // If not logged in, go to login page
        return "redirect:/login";
    }
}