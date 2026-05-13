package com.example.bijlibachat.dao;

import com.example.bijlibachat.model.ElectricityBill;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    private Connection conn = DatabaseConnection.getInstance().getConnection();

    public void saveBill(ElectricityBill bill) {
        String sql = "INSERT INTO electricity_bills (user_id, estimated_amount, billing_cycle, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bill.getUserID());
            ps.setFloat(2, bill.getEstimatedAmount());
            ps.setString(3, bill.getBillingCycle());
            ps.setString(4, bill.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void saveOrUpdateBill(ElectricityBill bill) {
        String sql = "UPDATE electricity_bills SET estimated_amount = ?, status = ? WHERE user_id = ? AND billing_cycle = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, bill.getEstimatedAmount());
            ps.setString(2, bill.getStatus());
            ps.setInt(3, bill.getUserID());
            ps.setString(4, bill.getBillingCycle());
            int updatedRows = ps.executeUpdate();
            if (updatedRows == 0) {
                saveBill(bill);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public ElectricityBill getLatestBill(int userID) {
        String sql = "SELECT TOP 1 * FROM electricity_bills WHERE user_id = ? ORDER BY bill_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ElectricityBill b = new ElectricityBill();
                b.setBillID(rs.getInt("bill_id"));
                b.setUserID(rs.getInt("user_id"));
                b.setEstimatedAmount(rs.getFloat("estimated_amount"));
                b.setBillingCycle(rs.getString("billing_cycle"));
                b.setStatus(rs.getString("status"));
                return b;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<ElectricityBill> getBillingHistory(int userID) {
        List<ElectricityBill> list = new ArrayList<>();
        String sql = "SELECT * FROM electricity_bills WHERE user_id = ? ORDER BY bill_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ElectricityBill b = new ElectricityBill();
                b.setBillID(rs.getInt("bill_id"));
                b.setUserID(rs.getInt("user_id"));
                b.setEstimatedAmount(rs.getFloat("estimated_amount"));
                b.setBillingCycle(rs.getString("billing_cycle"));
                b.setStatus(rs.getString("status"));
                list.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
