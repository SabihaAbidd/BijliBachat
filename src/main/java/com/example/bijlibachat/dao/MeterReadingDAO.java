package com.example.bijlibachat.dao;

import com.example.bijlibachat.model.MeterReading;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeterReadingDAO {
    private Connection conn = DatabaseConnection.getInstance().getConnection();

    public void save(MeterReading r) {
        String sql = "INSERT INTO meter_readings (user_id, value, reading_date, units_consumed) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getUserID());
            ps.setFloat(2, r.getValue());
            ps.setDate(3, new java.sql.Date(r.getDate().getTime()));
            ps.setFloat(4, r.getUnitsConsumed());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public MeterReading getLastReading(int userID) {
        String sql = "SELECT TOP 1 * FROM meter_readings WHERE user_id = ? AND reading_date <= CAST(GETDATE() AS DATE) ORDER BY reading_date DESC, reading_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                MeterReading r = new MeterReading();
                r.setReadingID(rs.getInt("reading_id"));
                r.setUserID(rs.getInt("user_id"));
                r.setValue(rs.getFloat("value"));
                r.setDate(rs.getDate("reading_date"));
                r.setUnitsConsumed(rs.getFloat("units_consumed"));
                return r;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<MeterReading> getReadingsByPeriod(int userID, Date start, Date end) {
        List<MeterReading> list = new ArrayList<>();
        String sql = "SELECT * FROM meter_readings WHERE user_id = ? AND reading_date BETWEEN ? AND ? ORDER BY reading_date ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ps.setDate(2, new java.sql.Date(start.getTime()));
            ps.setDate(3, new java.sql.Date(end.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MeterReading r = new MeterReading();
                r.setReadingID(rs.getInt("reading_id"));
                r.setUserID(rs.getInt("user_id"));
                r.setValue(rs.getFloat("value"));
                r.setDate(rs.getDate("reading_date"));
                r.setUnitsConsumed(rs.getFloat("units_consumed"));
                list.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public float getTotalUnitsThisCycle(int userID) {
        String sql = "SELECT SUM(units_consumed) as total FROM meter_readings WHERE user_id = ? AND reading_date <= CAST(GETDATE() AS DATE)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getFloat("total");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
