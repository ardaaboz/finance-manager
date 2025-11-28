package com.example.financemanager.repositories;

import com.example.financemanager.entities.Transaction;
import com.example.financemanager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Transaction entity data access.
 * Provides CRUD operations and custom query methods for filtering transactions.
 *
 * <p>Supports queries for:</p>
 * <ul>
 *   <li>All transactions by user</li>
 *   <li>Filtering by type (INCOME/EXPENSE) and category</li>
 *   <li>Recurring bills with various payment statuses</li>
 *   <li>Date-based filtering for upcoming bills</li>
 * </ul>
 *
 * @author Finance Manager Team
 * @version 1.0
 * @since 2025-01-28
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser(User user);
    List<Transaction> findByType(String type);

    // Filter methods
    List<Transaction> findByUserAndType(User user, String type);
    List<Transaction> findByUserAndCategory(User user, String category);
    List<Transaction> findByUserAndTypeAndCategory(User user, String type, String category);

    List<Transaction> findByUserAndIsRecurringTrue(User user);
    List<Transaction> findByUserAndIsRecurringTrueAndIsPaidFalse(User user);

    List<Transaction> findByUserAndIsRecurringTrueAndNextDueDateBetween(
            User user, LocalDate startDate, LocalDate endDate);

}