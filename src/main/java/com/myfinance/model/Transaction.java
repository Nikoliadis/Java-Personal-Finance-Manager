package com.myfinance.model;

import java.time.LocalDate;
import com.myfinance.model.Transaction;


public class Transaction {
    private String type;
    private String category;
    private double amount;
    private LocalDate date;

    // Constructor
    public Transaction(String type, String category, double amount, LocalDate date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
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

    @Override
    public String toString() {
        return "[" + type.toUpperCase() + "] " + category + " - " + amount + "€ (" + date + ")";
    }
}
