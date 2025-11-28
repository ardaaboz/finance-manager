package com.example.financemanager.config;

import com.example.financemanager.exceptions.TransactionNotFoundException;
import com.example.financemanager.exceptions.UnauthorizedAccessException;
import com.example.financemanager.exceptions.UserAlreadyExistsException;
import com.example.financemanager.exceptions.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global exception handler for the application.
 * Intercepts exceptions thrown by controllers and provides user-friendly error handling.
 *
 * This class handles:
 * - Custom application exceptions (UserNotFound, TransactionNotFound, etc.)
 * - Validation exceptions
 * - Generic runtime exceptions
 *
 * All exceptions are logged and users are redirected with appropriate error messages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles UserNotFoundException exceptions.
     * Redirects user to login page with an error message.
     *
     * @param ex the UserNotFoundException
     * @param redirectAttributes attributes for the redirect
     * @return redirect to login page
     */
    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException ex,
                                                RedirectAttributes redirectAttributes) {
        log.error("User not found: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/login";
    }

    /**
     * Handles TransactionNotFoundException exceptions.
     * Redirects user to dashboard with an error message.
     *
     * @param ex the TransactionNotFoundException
     * @param redirectAttributes attributes for the redirect
     * @return redirect to dashboard
     */
    @ExceptionHandler(TransactionNotFoundException.class)
    public String handleTransactionNotFoundException(TransactionNotFoundException ex,
                                                      RedirectAttributes redirectAttributes) {
        log.error("Transaction not found: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dashboard";
    }

    /**
     * Handles UnauthorizedAccessException exceptions.
     * Redirects user to dashboard with an error message.
     *
     * @param ex the UnauthorizedAccessException
     * @param redirectAttributes attributes for the redirect
     * @return redirect to dashboard
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public String handleUnauthorizedAccessException(UnauthorizedAccessException ex,
                                                     RedirectAttributes redirectAttributes) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dashboard";
    }

    /**
     * Handles UserAlreadyExistsException exceptions.
     * Redirects user to registration page with an error message.
     *
     * @param ex the UserAlreadyExistsException
     * @param redirectAttributes attributes for the redirect
     * @return redirect to registration page
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public String handleUserAlreadyExistsException(UserAlreadyExistsException ex,
                                                    RedirectAttributes redirectAttributes) {
        log.warn("User already exists: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/register";
    }

    /**
     * Handles ConstraintViolationException (validation errors).
     * Returns user to previous page with validation error messages.
     *
     * @param ex the ConstraintViolationException
     * @param redirectAttributes attributes for the redirect
     * @return redirect to referer or dashboard
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public String handleValidationException(ConstraintViolationException ex,
                                             RedirectAttributes redirectAttributes) {
        log.warn("Validation error: {}", ex.getMessage());
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Validation error occurred");
        redirectAttributes.addFlashAttribute("error", errorMessage);
        return "redirect:/dashboard";
    }

    /**
     * Handles all other unexpected exceptions.
     * Logs the error and redirects user to dashboard with a generic error message.
     *
     * @param ex the Exception
     * @param redirectAttributes attributes for the redirect
     * @return redirect to dashboard
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex,
                                          RedirectAttributes redirectAttributes) {
        log.error("Unexpected error occurred", ex);
        redirectAttributes.addFlashAttribute("error",
                "An unexpected error occurred. Please try again.");
        return "redirect:/dashboard";
    }
}
