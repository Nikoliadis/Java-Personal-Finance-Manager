package com.myfinance;

import com.myfinance.logic.TransactionManager;
import com.myfinance.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class MainApp {
    private static TransactionManager manager = new TransactionManager();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            manager.loadFromCSV();

            JFrame frame = new JFrame("Personal Finance Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);

            JButton addBtn = new JButton("Προσθήκη Συναλλαγής");
            JTextArea log = new JTextArea();

            addBtn.addActionListener(e -> {
                Transaction t = new Transaction("expense", "Φαγητό", 12.5, LocalDate.now());
                manager.addTransaction(t);
                log.append(t.toString() + "\n");
            });

            frame.setLayout(new BorderLayout());
            frame.add(addBtn, BorderLayout.NORTH);
            frame.add(new JScrollPane(log), BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
