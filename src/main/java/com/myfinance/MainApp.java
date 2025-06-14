package com.myfinance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MainApp {
    private static DefaultTableModel tableModel;
    private static JTable table;
    private static JLabel totalLabel;
    private static JComboBox<String> paymentCombo;

    // Stock simulation
    private static Map<String, Integer> stock = new HashMap<>() {{
        put("Καφές", 50);
        put("Νερό", 100);
        put("Χυμός", 40);
    }};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Προσθήκη login screen στο μέλλον
            showPOSWindow();
        });
    }

    private static void showPOSWindow() {
        JFrame frame = new JFrame("Mini POS - Ταμειακή Εφαρμογή");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 580);
        frame.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        JLabel productLabel = new JLabel("Προϊόν:");
        JTextField productField = new JTextField();

        JLabel priceLabel = new JLabel("Τιμή:");
        JTextField priceField = new JTextField();

        JLabel quantityLabel = new JLabel("Ποσότητα:");
        JTextField quantityField = new JTextField("1");

        JLabel paymentLabel = new JLabel("Τρόπος Πληρωμής:");
        String[] methods = {"Μετρητά", "Κάρτα", "PayPal"};
        paymentCombo = new JComboBox<>(methods);

        JButton addBtn = new JButton("Προσθήκη");
        JButton payBtn = new JButton("Ολοκλήρωση Πληρωμής");
        JButton reportBtn = new JButton("Αναφορά Ημέρας");
        JButton graphBtn = new JButton("📊 Στατιστικά");

        formPanel.add(productLabel);
        formPanel.add(productField);
        formPanel.add(priceLabel);
        formPanel.add(priceField);
        formPanel.add(quantityLabel);
        formPanel.add(quantityField);
        formPanel.add(paymentLabel);
        formPanel.add(paymentCombo);
        formPanel.add(addBtn);
        formPanel.add(payBtn);
        formPanel.add(reportBtn);
        formPanel.add(graphBtn);

        frame.add(formPanel, BorderLayout.NORTH);

        String[] columns = {"Προϊόν", "Τιμή", "Ποσότητα", "Σύνολο"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        totalLabel = new JLabel("Σύνολο: 0.00€", SwingConstants.RIGHT);
        frame.add(totalLabel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            try {
                String product = productField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int quantity = Integer.parseInt(quantityField.getText().trim());
                double total = price * quantity;

                // Έλεγχος αποθέματος
                if (stock.containsKey(product) && stock.get(product) < quantity) {
                    JOptionPane.showMessageDialog(frame, "Μη επαρκές απόθεμα για " + product, "Stock", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                tableModel.addRow(new Object[]{product, price, quantity, total});
                updateTotal();

                productField.setText("");
                priceField.setText("");
                quantityField.setText("1");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Λάθος τιμή ή ποσότητα", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        });

        payBtn.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(frame, "Δεν υπάρχουν προϊόντα για πληρωμή", "Προσοχή", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double totalSales = 0;
            StringBuilder receipt = new StringBuilder();
            receipt.append("ΑΠΟΔΕΙΞΗ\n");
            receipt.append("------------------------------\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String name = tableModel.getValueAt(i, 0).toString();
                String price = tableModel.getValueAt(i, 1).toString();
                String qty = tableModel.getValueAt(i, 2).toString();
                String sum = tableModel.getValueAt(i, 3).toString();
                totalSales += Double.parseDouble(sum);
                receipt.append(name).append(" x").append(qty).append(" -> ").append(sum).append("€\n");

                // Αφαίρεση απόθεματος
                if (stock.containsKey(name)) {
                    int q = Integer.parseInt(qty);
                    stock.put(name, stock.get(name) - q);
                }
            }

            receipt.append("------------------------------\n");
            receipt.append(totalLabel.getText()).append("\n");
            receipt.append("Τρόπος Πληρωμής: ").append(paymentCombo.getSelectedItem().toString()).append("\n");
            receipt.append("Ημερομηνία: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");

            saveReceiptToFile(receipt.toString());
            saveToDailyLog(totalSales);

            if (paymentCombo.getSelectedItem().toString().equals("PayPal")) {
                try {
                    String amountStr = String.format("%.2f", totalSales).replace(",", ".");
                    Desktop.getDesktop().browse(new URI("https://www.paypal.com/paypalme/YourName/" + amountStr));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            JOptionPane.showMessageDialog(frame, receipt.toString(), "Απόδειξη", JOptionPane.INFORMATION_MESSAGE);
            tableModel.setRowCount(0);
            updateTotal();
        });

        reportBtn.addActionListener(e -> showDailyReport(frame));
        graphBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "🚧 Έρχονται σύντομα γραφήματα πωλήσεων...", "Graph", JOptionPane.INFORMATION_MESSAGE));

        frame.setVisible(true);
    }

    private static void updateTotal() {
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += (double) tableModel.getValueAt(i, 3);
        }
        totalLabel.setText("Σύνολο: " + String.format("%.2f", total) + "€");
    }

    private static void saveReceiptToFile(String content) {
        try {
            File dir = new File("receipts");
            if (!dir.exists()) dir.mkdirs();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File file = new File(dir, "receipt_" + timestamp + ".txt");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveToDailyLog(double totalSales) {
        try {
            File dir = new File("reports");
            if (!dir.exists()) dir.mkdirs();
            String date = LocalDate.now().toString();
            File log = new File(dir, "report_" + date + ".log");
            try (FileWriter writer = new FileWriter(log, true)) {
                writer.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " - Πώληση: " + String.format("%.2f", totalSales) + "€\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showDailyReport(JFrame parent) {
        String date = LocalDate.now().toString();
        File log = new File("reports/report_" + date + ".log");
        if (!log.exists()) {
            JOptionPane.showMessageDialog(parent, "Δεν υπάρχουν πωλήσεις για σήμερα.", "Αναφορά", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(log));
            StringBuilder content = new StringBuilder("Αναφορά Πωλήσεων: " + date + "\n\n");
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            JOptionPane.showMessageDialog(parent, content.toString(), "Αναφορά", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
