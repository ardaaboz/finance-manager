package com.example.financemanager;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne  // Many transactions belong to one user
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String description;
    private double amount;  
    private String type;  
    private String category;

    private LocalDate dueDate;

    private boolean isRecurring;
    private Integer dayOfMonth;
    private LocalDate nextDueDate;
    private boolean isPaid;

    @CreationTimestamp
    private LocalDateTime createdDate;

    // Constructors
    public Transaction() {}

    // Recurring transaction
    public Transaction(User user, String description, double amount,
                       String type, String category, int dayOfMonth) {
        this.user = user;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.isRecurring = true;
        this.dayOfMonth = dayOfMonth;
        this.isPaid = false;
        this.nextDueDate = calculateNextDueDate(dayOfMonth);
    }

    // Regular transaction
    public Transaction(User user, String description, double amount,
                       String type, String category) {
        this.user = user;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.isRecurring = false;
    }

    // One-time transaction with due date
    public Transaction(User user, String description, double amount,
                       String type, String category, LocalDate dueDate) {
        this.user = user;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.dueDate = dueDate;
        this.isRecurring = false;
        this.isPaid = false;
    }


    private LocalDate calculateNextDueDate(int dayOfMonth) {
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

        return nextDue;
    }

    // Getters and setters

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public Integer getDayOfMonth() {
        return dayOfMonth;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setRecurring(boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
