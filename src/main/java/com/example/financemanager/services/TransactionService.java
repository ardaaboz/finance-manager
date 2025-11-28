package com.example.financemanager.services;

import com.example.financemanager.repositories.UserRepository;
import com.example.financemanager.entities.Transaction;
import com.example.financemanager.entities.User;
import com.example.financemanager.repositories.TransactionRepository;
import com.example.financemanager.exceptions.TransactionNotFoundException;
import com.example.financemanager.exceptions.UnauthorizedAccessException;
import com.example.financemanager.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing financial transactions and recurring bills.
 *
 * <p>This service handles all business logic related to transactions, including:</p>
 * <ul>
 *   <li>CRUD operations for all transaction types (regular, recurring, scheduled)</li>
 *   <li>Financial calculations (total income, expenses, balance)</li>
 *   <li>Bill management and payment tracking</li>
 *   <li>Transaction filtering by type and category</li>
 *   <li>Calendar generation for bills view</li>
 *   <li>Next due date calculations for recurring bills</li>
 * </ul>
 *
 * <p><b>Security Note:</b> All methods validate that the authenticated user
 * owns the transactions they're trying to access or modify. Unauthorized
 * access attempts throw {@link UnauthorizedAccessException}.</p>
 *
 * @author Finance Manager Team
 * @version 1.0
 * @since 2025-01-28
 * @see Transaction
 * @see User
 */
@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Helper method to retrieve a user by username with null safety.
     * Throws an exception if the user is not found.
     *
     * @param username the username to search for
     * @return the User entity
     * @throws UserNotFoundException if no user exists with the given username
     */
    private User getUserOrThrow(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw UserNotFoundException.forUsername(username);
        }
        return user;
    }

    /**
     * Retrieves all transactions for a specific user.
     *
     * @param username the username of the transaction owner
     * @return list of all transactions belonging to the user (may be empty)
     * @throws UserNotFoundException if the user doesn't exist
     */
    public List<Transaction> getUserTransactions(String username) {
        User user = getUserOrThrow(username);
        return transactionRepository.findByUser(user);
    }

    /**
     * Creates a new regular transaction (one-time, no due date).
     *
     * @param username the username of the transaction owner
     * @param description brief description of the transaction
     * @param amount transaction amount (must be positive)
     * @param type "INCOME" or "EXPENSE"
     * @param category transaction category (e.g., "Food", "Salary")
     * @throws UserNotFoundException if the user doesn't exist
     */
    public void createTransaction(String username, String description,
                                  double amount, String type, String category) {
        User user = getUserOrThrow(username);
        Transaction newTransaction = new Transaction(user, description, amount, type, category);
        transactionRepository.save(newTransaction);
    }

    /**
     * Creates a new recurring bill that repeats monthly on a specific day.
     *
     * @param username the username of the bill owner
     * @param description brief description of the bill
     * @param amount bill amount (must be positive)
     * @param type usually "EXPENSE" for bills
     * @param category bill category (e.g., "Rent", "Utilities")
     * @param dayOfMonth day of month when bill is due (1-31)
     * @throws UserNotFoundException if the user doesn't exist
     */
    public void createRecurringTransaction(String username, String description,
                                           double amount, String type, String category, int dayOfMonth) {
        User user = getUserOrThrow(username);
        Transaction newTransaction = new Transaction(user, description, amount, type, category, dayOfMonth);
        transactionRepository.save(newTransaction);
    }

    /**
     * Creates a one-time transaction with a specific due date.
     *
     * @param username the username of the transaction owner
     * @param description brief description of the transaction
     * @param amount transaction amount (must be positive)
     * @param type "INCOME" or "EXPENSE"
     * @param category transaction category
     * @param dueDate the specific date when this transaction is due
     * @throws UserNotFoundException if the user doesn't exist
     */
    public void createTransactionWithDueDate(String username, String description,
                                             double amount, String type, String category,
                                             LocalDate dueDate) {
        User user = getUserOrThrow(username);
        Transaction transaction = new Transaction(user, description, amount, type, category, dueDate);
        transactionRepository.save(transaction);
    }

    /**
     * Deletes a transaction.
     * Validates that the transaction belongs to the specified user.
     *
     * @param transactionId ID of the transaction to delete
     * @param username username of the user requesting deletion
     * @throws TransactionNotFoundException if the transaction doesn't exist
     * @throws UnauthorizedAccessException if the transaction doesn't belong to the user
     */
    public void deleteTransaction(Long transactionId, String username) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> TransactionNotFoundException.forId(transactionId));

        if (!transaction.getUser().getUsername().equals(username)) {
            throw UnauthorizedAccessException.forTransaction();
        }

        transactionRepository.delete(transaction);
    }

    // Get transaction by ID
    public Transaction getTransactionById(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> TransactionNotFoundException.forId(id));

        // Security check
        if (!transaction.getUser().getUsername().equals(username)) {
            throw UnauthorizedAccessException.forTransaction();
        }

        return transaction;
    }

    // Edit transaction
    public void updateTransaction(Long id, String username, String description,
                                  double amount, String type, String category,
                                  Boolean isRecurring, Integer dayOfMonth) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> TransactionNotFoundException.forId(id));

        // Security check
        if (!transaction.getUser().getUsername().equals(username)) {
            throw UnauthorizedAccessException.forTransaction();
        }

        // Update fields
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategory(category);

        // Handle recurring changes
        if (isRecurring != null && isRecurring && dayOfMonth != null) {
            transaction.setRecurring(true);
            transaction.setDayOfMonth(dayOfMonth);
            // Calculate next due date
            LocalDate today = LocalDate.now();
            int lastDayOfMonth = today.lengthOfMonth();
            int actualDay = Math.min(dayOfMonth, lastDayOfMonth);

            LocalDate nextDue = LocalDate.of(today.getYear(), today.getMonth(), actualDay);
            if (nextDue.isBefore(today) || nextDue.isEqual(today)) {
                nextDue = nextDue.plusMonths(1);

                int nextMonthLastDay = nextDue.lengthOfMonth();
                if (dayOfMonth > nextMonthLastDay) {
                    nextDue = nextDue.withDayOfMonth(nextMonthLastDay);
                } else {
                    nextDue = nextDue.withDayOfMonth(dayOfMonth);
                }
            }
            transaction.setNextDueDate(nextDue);
        } else {
            transaction.setRecurring(false);
            transaction.setNextDueDate(null);
            transaction.setDayOfMonth(null);
        }

        transactionRepository.save(transaction);
    }

    // Filter transactions
    public List<Transaction> getFilteredTransactions(String username, String type, String category) {
        User user = getUserOrThrow(username);

        // Filter by both type and category
        if (type != null && !type.isEmpty() && category != null && !category.isEmpty()) {
            return transactionRepository.findByUserAndTypeAndCategory(user, type, category);
        }
        // Filter for type only
        else if (type != null && !type.isEmpty()) {
            return transactionRepository.findByUserAndType(user, type);
        }
        // Filter for category only
        else if (category != null && !category.isEmpty()) {
            return transactionRepository.findByUserAndCategory(user, category);
        }
        // Return all if no filters exist
        else {
            return transactionRepository.findByUser(user);
        }
    }

    // Get all recurring bills for a user
    public List<Transaction> getRecurringBills(String username) {
        User user = getUserOrThrow(username);
        return transactionRepository.findByUserAndIsRecurringTrue(user);
    }

    // Get unpaid bills
    public List<Transaction> getUnpaidBills(String username) {
        User user = getUserOrThrow(username);
        return transactionRepository.findByUserAndIsRecurringTrueAndIsPaidFalse(user);
    }

    // Get bills due in the next X days
    public List<Transaction> getUpcomingBills(String username, int daysAhead) {
        User user = getUserOrThrow(username);
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);

        return transactionRepository.findByUserAndIsRecurringTrueAndNextDueDateBetween
                (user, today, futureDate);
    }

    // Mark transaction as paid (handles both recurring and one-time)
    public void markTransactionPaid(Long transactionId, String username) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> TransactionNotFoundException.forId(transactionId));

        if (!transaction.getUser().getUsername().equals(username)) {
            throw UnauthorizedAccessException.forTransaction();
        }

        transaction.setPaid(true);

        // If recurring, calculate next due date
        if (transaction.isRecurring()) {
            LocalDate currentDue = transaction.getNextDueDate();
            LocalDate nextDue = currentDue.plusMonths(1);

            int dayOfMonth = transaction.getDayOfMonth();
            int daysInNextMonth = nextDue.lengthOfMonth();

            if (dayOfMonth > daysInNextMonth) {
                nextDue = nextDue.withDayOfMonth(daysInNextMonth);
            } else {
                nextDue = nextDue.withDayOfMonth(dayOfMonth);
            }

            transaction.setNextDueDate(nextDue);
        }

        transactionRepository.save(transaction);
    }

    // Keep old method name for backward compatibility
    public void markBillAsPaid(Long transactionId, String username) {
        markTransactionPaid(transactionId, username);
    }

    // Reset bills at the start of new month
    public void resetMonthlyBills(String username) {
        User user = getUserOrThrow(username);
        List<Transaction> paidBills = transactionRepository.findByUserAndIsRecurringTrue(user);

        LocalDate today = LocalDate.now();

        for (Transaction bill : paidBills) {
            // If next due date is in the past, reset isPaid to false
            if (bill.getNextDueDate() != null && bill.getNextDueDate().isBefore(today)) {
                bill.setPaid(false);

                // Recalculate next due date
                int dayOfMonth = bill.getDayOfMonth();
                int currentMonthDays = today.lengthOfMonth();
                int actualDay = Math.min(dayOfMonth, currentMonthDays);

                LocalDate newDueDate = LocalDate.of(today.getYear(), today.getMonth(), actualDay);
                bill.setNextDueDate(newDueDate);

                transactionRepository.save(bill);
            }
        }
    }

    // Get bills filtered by status and time range
    public List<Transaction> getFilteredBills(String username, String filter) {
        User user = getUserOrThrow(username);
        List<Transaction> allBills = transactionRepository.findByUserAndIsRecurringTrue(user);
        LocalDate today = LocalDate.now();

        switch (filter) {
            case "upcoming7":
                LocalDate week = today.plusDays(7);
                return allBills.stream()
                        .filter(b -> !b.isPaid() && b.getNextDueDate() != null
                                && !b.getNextDueDate().isBefore(today)
                                && !b.getNextDueDate().isAfter(week))
                        .collect(Collectors.toList());

            case "upcoming30":
                LocalDate month = today.plusDays(30);
                return allBills.stream()
                        .filter(b -> !b.isPaid() && b.getNextDueDate() != null
                                && !b.getNextDueDate().isBefore(today)
                                && !b.getNextDueDate().isAfter(month))
                        .collect(Collectors.toList());

            case "overdue":
                return allBills.stream()
                        .filter(b -> !b.isPaid() && b.getNextDueDate() != null
                                && b.getNextDueDate().isBefore(today))
                        .collect(Collectors.toList());

            case "paid":
                return allBills.stream()
                        .filter(Transaction::isPaid)
                        .collect(Collectors.toList());

            case "unpaid":
                return allBills.stream()
                        .filter(b -> !b.isPaid())
                        .collect(Collectors.toList());

            default: // "all"
                return allBills;
        }
    }

    // Get all transactions for calendar (both recurring and one-time with due dates)
    public Map<Integer, List<Transaction>> getTransactionsCalendar(String username, int year, int month) {
        User user = getUserOrThrow(username);

        Map<Integer, List<Transaction>> calendar = new HashMap<>();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

        // Get recurring bills
        List<Transaction> recurringBills = transactionRepository.findByUserAndIsRecurringTrue(user);
        for (Transaction bill : recurringBills) {
            if (bill.getNextDueDate() != null) {
                LocalDate dueDate = bill.getNextDueDate();

                // Show in this month if due date is in this month
                if (!dueDate.isBefore(firstDay) && !dueDate.isAfter(lastDay)) {
                    int day = dueDate.getDayOfMonth();
                    calendar.computeIfAbsent(day, k -> new ArrayList<>()).add(bill);
                }
            }
        }

        // Get one-time transactions with due dates
        List<Transaction> allTransactions = transactionRepository.findByUser(user);
        for (Transaction trans : allTransactions) {
            if (!trans.isRecurring() && trans.getDueDate() != null) {
                LocalDate dueDate = trans.getDueDate();

                // Show in this month if due date is in this month
                if (!dueDate.isBefore(firstDay) && !dueDate.isAfter(lastDay)) {
                    int day = dueDate.getDayOfMonth();
                    calendar.computeIfAbsent(day, k -> new ArrayList<>()).add(trans);
                }
            }
        }

        return calendar;
    }

    // Keep old method name for backward compatibility
    public Map<Integer, List<Transaction>> getBillsCalendar(String username, int year, int month) {
        return getTransactionsCalendar(username, year, month);
    }

    // Calculate total income for a user
    public double getTotalIncome(String username) {
        User user = getUserOrThrow(username);
        List<Transaction> transactions = transactionRepository.findByUser(user);

        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("INCOME")) {
                total += t.getAmount();
            }
        }
        return total;
    }

    // Calculate total expenses for a user
    public double getTotalExpense(String username) {
        User user = getUserOrThrow(username);
        List<Transaction> transactions = transactionRepository.findByUser(user);

        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("EXPENSE")) {
                total += t.getAmount();
            }
        }
        return total;
    }

    // Calculate net balance
    public double getBalance(String username) {
        return getTotalIncome(username) - getTotalExpense(username);
    }
}