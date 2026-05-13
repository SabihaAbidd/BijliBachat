package com.example.bijlibachat.dao;

import com.example.bijlibachat.model.Appliance;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplianceDAO {
    private Connection conn = DatabaseConnection.getInstance().getConnection();

    public List<Appliance> getAppliancesByUser(int userID) {
        List<Appliance> list = new ArrayList<>();
        String sql = "SELECT * FROM appliances WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Appliance a = new Appliance();
                a.setApplianceID(rs.getInt("appliance_id"));
                a.setUserID(rs.getInt("user_id"));
                a.setName(rs.getString("name"));
                a.setWattage(rs.getFloat("wattage"));
                a.setHoursUsed(rs.getFloat("hours_used"));
                list.add(a);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean saveAppliance(Appliance a) {
        String sql = "INSERT INTO appliances (user_id, name, wattage, hours_used) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) {
                throw new SQLException("Database connection is not available.");
            }
            ps.setInt(1, a.getUserID());
            ps.setString(2, a.getName());
            ps.setFloat(3, a.getWattage());
            ps.setFloat(4, a.getHoursUsed());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save appliance: " + e.getMessage(), e);
        }
    }

    public void updateHoursUsed(int applianceID, float hours) {
        String sql = "UPDATE appliances SET hours_used = ? WHERE appliance_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, hours);
            ps.setInt(2, applianceID);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
