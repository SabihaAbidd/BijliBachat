package com.example.bijlibachat;

import com.example.bijlibachat.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("--- Starting Connection Test ---");

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ SQL Server is connected!");

                // Test query
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM users");
                while (rs.next()) {
                    System.out.println("User: " + rs.getString("name") +
                            " | Email: " + rs.getString("email"));
                }
                rs.close();
                stmt.close();

            } else {
                System.out.println("❌ ERROR: Connection object is null or closed.");
            }

        } catch (SQLException e) {
            System.out.println("❌ CRITICAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("--- Launching UI ---");
        HelloApplication.main(args);
    }
}