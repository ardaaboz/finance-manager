package com.example.financemanager.controllers;

import com.example.financemanager.entities.User;
import com.example.financemanager.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.security.Principal;
import java.util.Locale;

/**
 * Controller for handling language/locale changes.
 * Supports switching between English and Turkish languages.
 *
 * Features:
 * - Session-based language switching
 * - Persistent language preference stored in user profile
 * - Redirect back to referring page after language change
 */
@Controller
public class LanguageController {

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private UserRepository userRepository;

    /**
     * Handles language change requests.
     * Updates both the session locale and the user's preferred language in the database.
     *
     * @param lang the language code (en for English, tr for Turkish)
     * @param request the HTTP servlet request
     * @param principal the authenticated user principal
     * @return redirect to the referer page or dashboard if referer is not available
     */
    @PostMapping("/change-language")
    public String changeLanguage(@RequestParam String lang,
                                  HttpServletRequest request,
                                  Principal principal) {
        // Set locale in session
        Locale locale = "tr".equals(lang) ? new Locale("tr") : Locale.ENGLISH;
        localeResolver.setLocale(request, null, locale);

        // Update user's preferred language in database if user is logged in
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username);
            if (user != null) {
                user.setPreferredLanguage(lang);
                userRepository.save(user);
            }
        }

        // Redirect back to the page they came from
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }
}
