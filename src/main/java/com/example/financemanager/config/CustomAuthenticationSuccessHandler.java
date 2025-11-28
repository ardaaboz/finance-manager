package com.example.financemanager.config;

import com.example.financemanager.entities.User;
import com.example.financemanager.repositories.UserRepository;
import jakarta.servlet.ServletException;
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
     * Loads the user's preferred language from database and sets it in the session.
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

        // Set locale based on user's preferred language
        if (user != null && user.getPreferredLanguage() != null) {
            Locale locale = "tr".equals(user.getPreferredLanguage())
                    ? new Locale("tr")
                    : Locale.ENGLISH;
            localeResolver.setLocale(request, response, locale);
        }

        // Redirect to dashboard
        response.sendRedirect("/dashboard");
    }
}
