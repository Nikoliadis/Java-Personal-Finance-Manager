package com.myfinance.logic;

import com.myfinance.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionManagerDB {
    private static final String DB_URL = "jdbc:sqlite:data/finance.db";

    public TransactionManagerDB() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL
            )
        """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions (type, category, amount, date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, t.getType());
            pstmt.setString(2, t.getCategory());
            pstmt.setDouble(3, t.getAmount());
            pstmt.setString(4, t.getDate().toString());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Transaction> getAll() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT type, category, amount, date FROM transactions";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        LocalDate.parse(rs.getString("date"))
                );
                transactions.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public List<Transaction> getAllWithId() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT id, type, category, amount, date FROM transactions";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        LocalDate.parse(rs.getString("date"))
                );
                t.setId(rs.getInt("id"));
                transactions.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
