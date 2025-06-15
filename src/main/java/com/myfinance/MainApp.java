package com.myfinance;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Desktop;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
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

    private static Map<String, Integer> stock = new HashMap<>() {{
        put("Καφές", 50);
        put("Νερό", 100);
        put("Χυμός", 40);
    }};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showLoginDialog();
            showPOSWindow();
        });
    }

    private static void showLoginDialog() {
        while (true) {
            JTextField userField = new JTextField();
            JPasswordField passField = new JPasswordField();
            Object[] fields = {
                    "Username:", userField,
                    "Password:", passField
            };

            int option = JOptionPane.showConfirmDialog(null, fields, "Σύνδεση", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                if (username.equals("Admin") && password.equals("Admin")) {
                    break;
                } else {
                    JOptionPane.showMessageDialog(null, "❌ Λάθος στοιχεία! Ξαναπροσπάθησε", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.exit(0);
            }
        }
    }

    private static void showPOSWindow() {
        JFrame frame = new JFrame("Mini POS - Ταμειακή Εφαρμογή");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 580);
        frame.setLayout(new java.awt.BorderLayout());

        JPanel formPanel = new JPanel(new java.awt.GridLayout(7, 2, 10, 10));
        JTextField productField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField("1");

        String[] methods = {"Μετρητά", "Κάρτα", "PayPal"};
        paymentCombo = new JComboBox<>(methods);

        JButton addBtn = new JButton("Προσθήκη");
        JButton payBtn = new JButton("Ολοκλήρωση Πληρωμής");
        JButton reportBtn = new JButton("Αναφορά Ημέρας");
        JButton graphBtn = new JButton("📊 Στατιστικά");

        formPanel.add(new JLabel("Προϊόν:"));
        formPanel.add(productField);
        formPanel.add(new JLabel("Τιμή:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Ποσότητα:"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel("Τρόπος Πληρωμής:"));
        formPanel.add(paymentCombo);
        formPanel.add(addBtn);
        formPanel.add(payBtn);
        formPanel.add(reportBtn);
        formPanel.add(graphBtn);

        frame.add(formPanel, java.awt.BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Προϊόν", "Τιμή", "Ποσότητα", "Σύνολο"}, 0);
        table = new JTable(tableModel);
        frame.add(new JScrollPane(table), java.awt.BorderLayout.CENTER);

        totalLabel = new JLabel("Σύνολο: 0.00€", SwingConstants.RIGHT);
        frame.add(totalLabel, java.awt.BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {
            try {
                String product = productField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int quantity = Integer.parseInt(quantityField.getText().trim());
                double total = price * quantity;

                if (stock.containsKey(product) && stock.get(product) < quantity) {
                    JOptionPane.showMessageDialog(frame, "Μη επαρκές απόθεμα", "Σφάλμα", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                tableModel.addRow(new Object[]{product, price, quantity, total});
                updateTotal();

                productField.setText("");
                priceField.setText("");
                quantityField.setText("1");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Λάθος είσοδος", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        });

        payBtn.addActionListener(e -> {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(frame, "Δεν υπάρχουν προϊόντα", "Σφάλμα", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double totalSales = 0;
            StringBuilder receipt = new StringBuilder();
            receipt.append("------------------------------\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String name = tableModel.getValueAt(i, 0).toString();
                String qty = tableModel.getValueAt(i, 2).toString();
                String sum = tableModel.getValueAt(i, 3).toString();
                receipt.append(name).append(" x").append(qty).append(" -> ").append(sum).append("€\n");
                totalSales += Double.parseDouble(sum);
            }

            receipt.append("------------------------------\n");
            receipt.append("Σύνολο: ").append(String.format("%.2f", totalSales)).append("€\n");
            receipt.append("Τρόπος Πληρωμής: ").append(paymentCombo.getSelectedItem()).append("\n");
            receipt.append("Ημερομηνία: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");

            saveReceiptToFile(receipt.toString());
            generateQR("https://yourdomain.com/receipt?id=12345");
            saveReceiptAsPDF(receipt.toString());
            saveToDailyLog(totalSales);

            tableModel.setRowCount(0);
            updateTotal();
        });

        reportBtn.addActionListener(e -> showDailyReport(frame));
        graphBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, "📊 Έρχονται σύντομα γραφήματα πωλήσεων...", "Graph", JOptionPane.INFORMATION_MESSAGE));

        frame.setVisible(true);
    }

    private static void generateQR(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 150, 150);
            Path path = FileSystems.getDefault().getPath("src/main/resources/qrcode.png");
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);
        } catch (IOException | WriterException e) {
            e.printStackTrace();
        }
    }

    private static void saveReceiptToFile(String content) {
        try {
            File dir = new File("receipts");
            if (!dir.exists()) dir.mkdirs();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            FileWriter fw = new FileWriter(new File(dir, "receipt_" + timestamp + ".txt"));
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveReceiptAsPDF(String content) {
        try {
            File dir = new File("receipts");
            if (!dir.exists()) dir.mkdirs();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File file = new File(dir, "receipt_" + timestamp + ".pdf");

            Document doc = new Document(new Rectangle(226, 750));
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            BaseFont base = BaseFont.createFont("src/main/resources/fonts/DejaVuSansMono.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font normalFont = new Font(base, 9);
            Font titleFont = new Font(base, 12, Font.BOLD);

            Image logo = Image.getInstance(MainApp.class.getResource("/logo.jpg"));
            logo.scaleToFit(80, 80);
            logo.setAlignment(Element.ALIGN_CENTER);
            doc.add(logo);

            Paragraph title = new Paragraph("ΑΠΟΔΕΙΞΗ ΛΙΑΝΙΚΗΣ ΠΩΛΗΣΗΣ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph info1 = new Paragraph("ΑΦΜ: 123456789", normalFont);
            Paragraph info2 = new Paragraph("POS1", normalFont);
            Paragraph info3 = new Paragraph("Ημερομηνία: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont);

            info1.setAlignment(Element.ALIGN_CENTER);
            info2.setAlignment(Element.ALIGN_CENTER);
            info3.setAlignment(Element.ALIGN_CENTER);

            doc.add(info1);
            doc.add(info2);
            doc.add(info3);
            doc.add(new Paragraph(" "));

            for (String line : content.split("\\n")) {
                Paragraph p = new Paragraph(line, normalFont);
                p.setAlignment(Element.ALIGN_CENTER);
                doc.add(p);
            }

            doc.add(new Paragraph(" "));

            Image qr = Image.getInstance("src/main/resources/qrcode.png");
            qr.scaleToFit(80, 80);
            qr.setAlignment(Element.ALIGN_CENTER);
            doc.add(qr);

            Paragraph footer = new Paragraph("© v1.0", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();

            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateTotal() {
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++)
            total += (double) tableModel.getValueAt(i, 3);
        totalLabel.setText("Σύνολο: " + String.format("%.2f", total) + "€");
    }

    private static void saveToDailyLog(double totalSales) {
        try {
            File dir = new File("reports");
            if (!dir.exists()) dir.mkdirs();
            FileWriter fw = new FileWriter(new File(dir, "report_" + LocalDate.now() + ".log"), true);
            fw.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " - Πώληση: " + String.format("%.2f", totalSales) + "€\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showDailyReport(JFrame frame) {
        File file = new File("reports/report_" + LocalDate.now() + ".log");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(frame, "Δεν υπάρχουν πωλήσεις για σήμερα.");
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                content.append(line).append("\n");
            br.close();
            JOptionPane.showMessageDialog(frame, content.toString(), "Αναφορά Πωλήσεων", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
