package com.example.financemanager.exceptions;

/**
 * Exception thrown when a user attempts to access or modify a resource they don't own.
 * This is used to enforce data isolation between users in the finance management system.
 */
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedAccessException with a default message.
     */
    public UnauthorizedAccessException() {
        super("Unauthorized: You do not have permission to access this resource");
    }

    /**
     * Constructs a new UnauthorizedAccessException with a custom message.
     *
     * @param message the detail message
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    /**
     * Constructs an exception for unauthorized transaction access.
     *
     * @return a new UnauthorizedAccessException with a message about transaction ownership
     */
    public static UnauthorizedAccessException forTransaction() {
        return new UnauthorizedAccessException("Unauthorized: This is not your transaction");
    }
}
