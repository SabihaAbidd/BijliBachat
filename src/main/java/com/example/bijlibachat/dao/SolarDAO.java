package com.example.bijlibachat.dao;

import com.example.bijlibachat.model.SolarMeter;
import java.sql.*;

public class SolarDAO {
    private Connection conn = DatabaseConnection.getInstance().getConnection();

    public void saveSolarReading(SolarMeter s) {
        String sql = "INSERT INTO solar_meters (user_id, generation_reading, exported_units, net_credit, reading_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getUserID());
            ps.setFloat(2, s.getGenerationReading());
            ps.setFloat(3, s.getExportedUnits());
            ps.setFloat(4, s.getNetCredit());
            ps.setDate(5, new java.sql.Date(s.getReadingDate().getTime()));
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public SolarMeter getLatestSolarReading(int userID) {
        String sql = "SELECT TOP 1 * FROM solar_meters WHERE user_id = ? ORDER BY solar_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SolarMeter s = new SolarMeter();
                s.setSolarID(rs.getInt("solar_id"));
                s.setUserID(rs.getInt("user_id"));
                s.setGenerationReading(rs.getFloat("generation_reading"));
                s.setExportedUnits(rs.getFloat("exported_units"));
                s.setNetCredit(rs.getFloat("net_credit"));
                s.setReadingDate(rs.getDate("reading_date"));
                return s;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
