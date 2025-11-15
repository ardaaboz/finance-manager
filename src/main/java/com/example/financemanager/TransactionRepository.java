package com.example.financemanager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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