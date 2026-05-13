package com.example.bijlibachat.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Properties properties = loadDatabaseProperties();
            String connectionString = requireProperty(properties, "db.connectionString");
            String username = requireProperty(properties, "db.username");
            String password = requireProperty(properties, "db.password");
            connection = DriverManager.getConnection(connectionString, username, password);
            System.out.println("Connected to bijli_bachat!");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Database configuration error: " + e.getMessage());
            connection = null;
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            connection = null;
        }
    }


    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private Properties loadDatabaseProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("config.properties was not found on the classpath.");
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read config.properties.", e);
        }
    }

    private String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value.trim();
    }
}
