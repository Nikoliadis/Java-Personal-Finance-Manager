package com.myfinance;

import com.myfinance.logic.TransactionManagerDB;
import com.myfinance.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class MainApp {
    private static TransactionManagerDB manager = new TransactionManagerDB();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Personal Finance Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 450);
            frame.setLayout(new BorderLayout());

            JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
            JLabel typeLabel = new JLabel("Τύπος:");
            String[] types = {"income", "expense"};
            JComboBox<String> typeCombo = new JComboBox<>(types);

            JLabel categoryLabel = new JLabel("Κατηγορία:");
            JTextField categoryField = new JTextField();

            JLabel amountLabel = new JLabel("Ποσό:");
            JTextField amountField = new JTextField();

            JButton addBtn = new JButton("Καταχώρηση Συναλλαγής");
            JTextArea log = new JTextArea();
            log.setEditable(false);

            formPanel.add(typeLabel);
            formPanel.add(typeCombo);
            formPanel.add(categoryLabel);
            formPanel.add(categoryField);
            formPanel.add(amountLabel);
            formPanel.add(amountField);
            formPanel.add(new JLabel());
            formPanel.add(addBtn);

            frame.add(formPanel, BorderLayout.NORTH);
            frame.add(new JScrollPane(log), BorderLayout.CENTER);


            for (Transaction t : manager.getAll()) {
                log.append(t.toString() + "\n");
            }

            addBtn.addActionListener(e -> {
                try {
                    String type = (String) typeCombo.getSelectedItem();
                    String category = categoryField.getText().trim();
                    double amount = Double.parseDouble(amountField.getText().trim());
                    LocalDate date = LocalDate.now();

                    Transaction t = new Transaction(type, category, amount, date);
                    manager.addTransaction(t);
                    log.append(t.toString() + "\n");

                    categoryField.setText("");
                    amountField.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Μη έγκυρο ποσό", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                }
            });

            frame.setVisible(true);
        });
    }
}
