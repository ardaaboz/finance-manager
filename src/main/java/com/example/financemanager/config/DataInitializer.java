package com.example.financemanager.config;

import com.example.financemanager.entities.Transaction;
import com.example.financemanager.entities.User;
import com.example.financemanager.repositories.TransactionRepository;
import com.example.financemanager.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void InitializeData() {
        // Only initialize if no users exist (prevent duplicates on restart)
        if (userRepository.count() > 0) {
            System.out.println("Data already exists, skipping initialization");
            return;
        }

        // Create test users
        User john = new User("john", "john@email.com", passwordEncoder.encode("password123"));
        User mary = new User("mary", "mary@email.com", passwordEncoder.encode("password456"));

        userRepository.save(john);
        userRepository.save(mary);

        // Create regular transactions (no due dates)
        Transaction johnSalary = new Transaction(john, "Monthly Salary", 3000.0, "INCOME", "Salary");
        Transaction johnCoffee = new Transaction(john, "Coffee", 4.50, "EXPENSE", "Food");

        // Create recurring bills for John
        Transaction johnRent = new Transaction(john, "Rent Payment", 1200.0, "EXPENSE", "Rent", 1); // Due on 1st
        Transaction johnUtilities = new Transaction(john, "Electric Bill", 120.0, "EXPENSE", "Utilities", 15); // Due on 15th
        Transaction johnInternet = new Transaction(john, "Internet Bill", 80.0, "EXPENSE", "Utilities", 20); // Due on 20th

        // Create one-time transactions with due dates for John
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        Transaction johnLoanPayment = new Transaction(john, "Loan Repayment", 500.0, "EXPENSE", "Loan", nextWeek);
        Transaction johnClientPayment = new Transaction(john, "Client Payment Due", 1500.0, "INCOME", "Freelance", nextMonth);

        // Create transactions for Mary
        Transaction maryFreelance = new Transaction(mary, "Freelance Work", 500.0, "INCOME", "Freelance");
        Transaction maryGroceries = new Transaction(mary, "Groceries", 85.30, "EXPENSE", "Food");

        // Create recurring bills for Mary
        Transaction maryRent = new Transaction(mary, "Monthly Rent", 1000.0, "EXPENSE", "Rent", 1); // Due on 1st
        Transaction maryGym = new Transaction(mary, "Gym Membership", 50.0, "EXPENSE", "Healthcare", 10); // Due on 10th

        // Create one-time transaction with due date for Mary
        LocalDate maryDeadline = LocalDate.now().plusDays(14);
        Transaction maryBorrowedMoney = new Transaction(mary, "Pay Back Sarah", 200.0, "EXPENSE", "Loan", maryDeadline);

        // Save all transactions
        transactionRepository.save(johnSalary);
        transactionRepository.save(johnCoffee);
        transactionRepository.save(johnRent);
        transactionRepository.save(johnUtilities);
        transactionRepository.save(johnInternet);
        transactionRepository.save(johnLoanPayment);
        transactionRepository.save(johnClientPayment);

        transactionRepository.save(maryFreelance);
        transactionRepository.save(maryGroceries);
        transactionRepository.save(maryRent);
        transactionRepository.save(maryGym);
        transactionRepository.save(maryBorrowedMoney);

        System.out.println("✅ Sample data created successfully!");
        System.out.println("📧 Test users:");
        System.out.println("   - john / password123");
        System.out.println("   - mary / password456");
    }
}