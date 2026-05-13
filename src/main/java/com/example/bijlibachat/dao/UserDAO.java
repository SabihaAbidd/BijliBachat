package com.example.bijlibachat.dao;

import com.example.bijlibachat.model.User;
import java.sql.*;

public class UserDAO {
    private Connection conn = DatabaseConnection.getInstance().getConnection();

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserID(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setMail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setPhone(rs.getString("phone"));
                u.setAccountType(rs.getString("account_type"));
                return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void createUser(User u) {
        String sql = "INSERT INTO users (name, email, password, phone, account_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getMail());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getAccountType());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateUser(User u) {
        String sql = "UPDATE users SET name=?, phone=?, account_type=? WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getPhone());
            ps.setString(3, u.getAccountType());
            ps.setInt(4, u.getUserID());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updatePassword(int userID, String hashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userID);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public User findByID(int userID) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserID(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setMail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setPhone(rs.getString("phone"));
                u.setAccountType(rs.getString("account_type"));
                return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}
