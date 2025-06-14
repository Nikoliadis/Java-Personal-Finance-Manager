package com.myfinance.model;

import java.time.LocalDate;

public class Transaction {
    private int id; // Χρησιμοποιείται για SQLite primary key
    private String type;
    private String category;
    private double amount;
    private LocalDate date;

    // Constructor χωρίς id (για προσθήκη)
    public Transaction(String type, String category, double amount, LocalDate date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "[" + type.toUpperCase() + "] " + category + " - " + amount + "€ (" + date + ")";
    }
}
