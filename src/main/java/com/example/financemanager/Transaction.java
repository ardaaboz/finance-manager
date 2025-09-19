package com.example.financemanager;

import jakarta.persistence.*;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne // Many transactions belong to one user
    private User user;

    private String description;
    private double amount;
    private String type;

    // Constructors
    public Transaction() {}

    public Transaction(User user, String description, double amount, String type) {
        this.user = user;
        this.description = description;
        this.amount = amount;
        this.type = type;
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
}
