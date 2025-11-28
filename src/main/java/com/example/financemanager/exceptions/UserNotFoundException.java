package com.example.financemanager.exceptions;

/**
 * Exception thrown when a requested user cannot be found in the system.
 * This typically occurs when attempting to perform operations with an invalid username.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new UserNotFoundException with a default message.
     */
    public UserNotFoundException() {
        super("User not found");
    }

    /**
     * Constructs a new UserNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new UserNotFoundException for a specific username.
     *
     * @param username the username that was not found
     * @return a new UserNotFoundException with details about the missing user
     */
    public static UserNotFoundException forUsername(String username) {
        return new UserNotFoundException("User not found: " + username);
    }
}
