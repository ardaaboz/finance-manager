package com.example.financemanager.config;

import com.example.financemanager.entities.User;
import com.example.financemanager.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Locale;

/**
 * Custom authentication success handler that sets the user's preferred language
 * from their profile when they log in.
 *
 * <p>This ensures that users see the application in their preferred language
 * immediately after logging in, based on their saved preference in the database.</p>
 *
 * @author Finance Manager Team
 * @version 1.0
 * @since 2025-01-28
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private UserRepository userRepository;

    /**
     * Called when a user successfully authenticates.
     * Loads the user's preferred language from cookie, database, or session.
     * Priority: Browser Cookie > Current Session > Database Preference > Default (en)
     *
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the Authentication object which was created during authentication
     * @throws IOException if an input or output exception occurs
     * @throws ServletException if a servlet exception occurs
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        // Get the authenticated username
        String username = authentication.getName();

        // Load user from database
        User user = userRepository.findByUsername(username);

        // Check for language preference in this order:
        // 1. Browser cookie (most recent user choice)
        // 2. Current session locale (what they just selected on login page)
        // 3. Database preference
        // 4. Default to English

        String preferredLang = null;

        // Check cookie first
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("preferredLanguage".equals(cookie.getName())) {
                    preferredLang = cookie.getValue();
                    break;
                }
            }
        }

        // If no cookie, check current session locale
        if (preferredLang == null) {
            Locale currentLocale = localeResolver.resolveLocale(request);
            if (currentLocale != null && "tr".equals(currentLocale.getLanguage())) {
                preferredLang = "tr";
            }
        }

        // If still no preference, use database
        if (preferredLang == null && user != null && user.getPreferredLanguage() != null) {
            preferredLang = user.getPreferredLanguage();
        }

        // Default to English if nothing set
        if (preferredLang == null) {
            preferredLang = "en";
        }

        // Set locale in session
        Locale locale = "tr".equals(preferredLang) ? new Locale("tr") : Locale.ENGLISH;
        localeResolver.setLocale(request, response, locale);

        // Redirect to dashboard
        response.sendRedirect("/dashboard");
    }
}
