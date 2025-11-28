package com.example.financemanager.exceptions;

/**
 * Exception thrown when attempting to create a user that already exists in the system.
 * This prevents duplicate usernames or emails in the database.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new UserAlreadyExistsException with a default message.
     */
    public UserAlreadyExistsException() {
        super("User already exists");
    }

    /**
     * Constructs a new UserAlreadyExistsException with a custom message.
     *
     * @param message the detail message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs an exception for a duplicate username.
     *
     * @param username the username that already exists
     * @return a new UserAlreadyExistsException with details about the duplicate
     */
    public static UserAlreadyExistsException forUsername(String username) {
        return new UserAlreadyExistsException("Username already exists: " + username);
    }

    /**
     * Constructs an exception for a duplicate email.
     *
     * @param email the email that already exists
     * @return a new UserAlreadyExistsException with details about the duplicate
     */
    public static UserAlreadyExistsException forEmail(String email) {
        return new UserAlreadyExistsException("Email already exists: " + email);
    }
}
