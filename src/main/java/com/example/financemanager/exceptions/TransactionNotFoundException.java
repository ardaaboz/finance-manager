package com.example.financemanager.exceptions;

/**
 * Exception thrown when a requested transaction cannot be found in the system.
 * This typically occurs when attempting to access, update, or delete a non-existent transaction.
 */
public class TransactionNotFoundException extends RuntimeException {

    /**
     * Constructs a new TransactionNotFoundException with a default message.
     */
    public TransactionNotFoundException() {
        super("Transaction not found");
    }

    /**
     * Constructs a new TransactionNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public TransactionNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new TransactionNotFoundException for a specific transaction ID.
     *
     * @param transactionId the ID of the transaction that was not found
     * @return a new TransactionNotFoundException with details about the missing transaction
     */
    public static TransactionNotFoundException forId(Long transactionId) {
        return new TransactionNotFoundException("Transaction not found with ID: " + transactionId);
    }
}
