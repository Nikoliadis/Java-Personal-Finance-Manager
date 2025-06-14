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
    private static JTable table;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Personal Finance Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(750, 500);
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
            JButton deleteBtn = new JButton("Διαγραφή Επιλεγμένης");

            formPanel.add(typeLabel);
            formPanel.add(typeCombo);
            formPanel.add(categoryLabel);
            formPanel.add(categoryField);
            formPanel.add(amountLabel);
            formPanel.add(amountField);
            formPanel.add(addBtn);
            formPanel.add(deleteBtn);

            frame.add(formPanel, BorderLayout.NORTH);

            String[] columnNames = {"ID", "Τύπος", "Κατηγορία", "Ποσό", "Ημερομηνία"};
            tableModel = new DefaultTableModel(columnNames, 0) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table = new JTable(tableModel);
            table.getColumnModel().getColumn(0).setMinWidth(0);
            table.getColumnModel().getColumn(0).setMaxWidth(0);
            table.getColumnModel().getColumn(0).setWidth(0);

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

            deleteBtn.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int id = (int) tableModel.getValueAt(selectedRow, 0);
                    manager.deleteTransaction(id);
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(frame, "Επιλέξτε μία συναλλαγή για διαγραφή.", "Προσοχή", JOptionPane.WARNING_MESSAGE);
                }
            });

            frame.setVisible(true);
        });
    }

    private static void refreshTable() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = manager.getAllWithId();
        for (Transaction t : transactions) {
            Object[] row = {
                    t.getId(),
                    t.getType(),
                    t.getCategory(),
                    t.getAmount(),
                    t.getDate()
            };
            tableModel.addRow(row);
        }
    }
}
