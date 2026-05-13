package com.example.bijlibachat.dao;

import com.example.bijlibachat.model.MonthlyBudget;
import java.sql.*;

public class BudgetDAO {
    private Connection conn = DatabaseConnection.getInstance().getConnection();

    public void saveBudget(MonthlyBudget b) {
        String sql = "INSERT INTO monthly_budgets (user_id, ceiling_amount, month) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, b.getUserID());
            ps.setFloat(2, b.getCeilingAmount());
            ps.setString(3, b.getMonth());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public MonthlyBudget getBudgetByUserAndMonth(int userID, String month) {
        String sql = "SELECT TOP 1 * FROM monthly_budgets WHERE user_id = ? AND month = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ps.setString(2, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                MonthlyBudget b = new MonthlyBudget();
                b.setBudgetID(rs.getInt("budget_id"));
                b.setUserID(rs.getInt("user_id"));
                b.setCeilingAmount(rs.getFloat("ceiling_amount"));
                b.setMonth(rs.getString("month"));
                return b;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateBudget(MonthlyBudget b) {
        String sql = "UPDATE monthly_budgets SET ceiling_amount = ? WHERE user_id = ? AND month = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, b.getCeilingAmount());
            ps.setInt(2, b.getUserID());
            ps.setString(3, b.getMonth());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
