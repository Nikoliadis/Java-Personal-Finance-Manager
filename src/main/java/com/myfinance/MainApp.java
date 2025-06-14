package com.myfinance;

import com.myfinance.logic.TransactionManagerDB;
import com.myfinance.model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class MainApp {
    private static TransactionManagerDB manager = new TransactionManagerDB();
    private static DefaultTableModel tableModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Personal Finance Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 500);
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

            formPanel.add(typeLabel);
            formPanel.add(typeCombo);
            formPanel.add(categoryLabel);
            formPanel.add(categoryField);
            formPanel.add(amountLabel);
            formPanel.add(amountField);
            formPanel.add(new JLabel());
            formPanel.add(addBtn);

            frame.add(formPanel, BorderLayout.NORTH);

            String[] columnNames = {"Τύπος", "Κατηγορία", "Ποσό", "Ημερομηνία"};
            tableModel = new DefaultTableModel(columnNames, 0);
            JTable table = new JTable(tableModel);
            JScrollPane tableScroll = new JScrollPane(table);
            frame.add(tableScroll, BorderLayout.CENTER);

            refreshTable();

            addBtn.addActionListener(e -> {
                try {
                    String type = (String) typeCombo.getSelectedItem();
                    String category = categoryField.getText().trim();
                    double amount = Double.parseDouble(amountField.getText().trim());
                    LocalDate date = LocalDate.now();

                    Transaction t = new Transaction(type, category, amount, date);
                    manager.addTransaction(t);
                    refreshTable();

                    categoryField.setText("");
                    amountField.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Μη έγκυρο ποσό", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                }
            });

            frame.setVisible(true);
        });
    }

    private static void refreshTable() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = manager.getAll();
        for (Transaction t : transactions) {
            Object[] row = {
                    t.getType(),
                    t.getCategory(),
                    t.getAmount(),
                    t.getDate()
            };
            tableModel.addRow(row);
        }
    }
}
