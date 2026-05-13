package com.example.bijlibachat.dao;

import com.example.bijlibachat.model.TariffRate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TariffDAO {
    private Connection conn = DatabaseConnection.getInstance().getConnection();

    public List<TariffRate> getAllTariffs() {
        List<TariffRate> list = new ArrayList<>();
        String sql = "SELECT TOP 4 * FROM tariff_rates ORDER BY tariff_id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TariffRate t = new TariffRate();
                t.setTariffID(rs.getInt("tariff_id"));
                t.setTierName(rs.getString("tier_name"));
                t.setRatePerUnit(rs.getFloat("rate_per_unit"));
                t.setMinUnits(rs.getInt("min_units"));
                t.setMaxUnits(rs.getInt("max_units"));
                list.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public TariffRate getTariffForUnits(float units) {
        String sql = "SELECT TOP 1 * FROM tariff_rates WHERE min_units <= ? AND max_units >= ? ORDER BY tariff_id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, units);
            ps.setFloat(2, units);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                TariffRate t = new TariffRate();
                t.setTariffID(rs.getInt("tariff_id"));
                t.setTierName(rs.getString("tier_name"));
                t.setRatePerUnit(rs.getFloat("rate_per_unit"));
                t.setMinUnits(rs.getInt("min_units"));
                t.setMaxUnits(rs.getInt("max_units"));
                return t;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateTariff(TariffRate t) {
        String sql = "UPDATE tariff_rates SET rate_per_unit = ?, last_updated = CAST(GETDATE() AS DATE) WHERE tariff_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setFloat(1, t.getRatePerUnit());
            ps.setInt(2, t.getTariffID());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
