package com.myfinance.logic;

import com.myfinance.model.Transaction;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class TransactionManager {
    private List<Transaction> transactions = new ArrayList<>();
    private final File csvFile = new File("transactions.csv");

    public void addTransaction(Transaction t) {
        transactions.add(t);
        saveToCSV();
    }

    public List<Transaction> getAll() {
        return transactions;
    }

    private void saveToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            for (Transaction t : transactions) {
                pw.println(t.getType() + "," + t.getCategory() + "," + t.getAmount() + "," + t.getDate());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromCSV() {
        if (!csvFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                Transaction t = new Transaction(
                        parts[0],
                        parts[1],
                        Double.parseDouble(parts[2]),
                        LocalDate.parse(parts[3])
                );
                transactions.add(t);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
