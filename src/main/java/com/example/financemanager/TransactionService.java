package com.example.financemanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all transactions for a specific user
    public List<Transaction> getUserTransactions(String username) {
        User user = userRepository.findByUsername(username);
        return transactionRepository.findByUser(user);
    }

    // Create new transaction
    public void createTransaction(String username, String description,
                                  double amount, String type, String category) {
        User user = userRepository.findByUsername(username);
        Transaction newTransaction = new Transaction(user, description, amount, type, category);
        transactionRepository.save(newTransaction);
    }

    // Create new recurring transaction
    public void createRecurringTransaction(String username, String description,
                                           double amount, String type, String category, int dayOfMonth) {
        User user = userRepository.findByUsername(username);
        Transaction newTransaction = new Transaction(user, description, amount, type, category, dayOfMonth);
        transactionRepository.save(newTransaction);
    }

    // Create one-time transaction with due date
    public void createTransactionWithDueDate(String username, String description,
                                             double amount, String type, String category,
                                             LocalDate dueDate) {
        User user = userRepository.findByUsername(username);
        Transaction transaction = new Transaction(user, description, amount, type, category, dueDate);
        transactionRepository.save(transaction);
    }

    // Delete a transaction
    public void deleteTransaction(Long transactionId, String username) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: This is not your transaction!");
        }

        transactionRepository.delete(transaction);
    }

    // Get transaction by ID
    public Transaction getTransactionById(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Security check
        if (!transaction.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
        }

        return transaction;
    }

    // Edit transaction
    public void updateTransaction(Long id, String username, String description,
                                  double amount, String type, String category,
                                  Boolean isRecurring, Integer dayOfMonth) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Security check
        if (!transaction.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
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
        User user = userRepository.findByUsername(username);

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
        User user = userRepository.findByUsername(username);
        return transactionRepository.findByUserAndIsRecurringTrue(user);
    }

    // Get unpaid bills
    public List<Transaction> getUnpaidBills(String username) {
        User user = userRepository.findByUsername(username);
        return transactionRepository.findByUserAndIsRecurringTrueAndIsPaidFalse(user);
    }

    // Get bills due in the next X days
    public List<Transaction> getUpcomingBills(String username, int daysAhead) {
        User user = userRepository.findByUsername(username);
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);

        return transactionRepository.findByUserAndIsRecurringTrueAndNextDueDateBetween
                (user, today, futureDate);
    }

    // Mark transaction as paid (handles both recurring and one-time)
    public void markTransactionPaid(Long transactionId, String username) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized");
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
        User user = userRepository.findByUsername(username);
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
        User user = userRepository.findByUsername(username);
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
        User user = userRepository.findByUsername(username);

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
        User user = userRepository.findByUsername(username);
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
        User user = userRepository.findByUsername(username);
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