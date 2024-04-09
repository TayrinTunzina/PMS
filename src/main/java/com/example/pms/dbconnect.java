package com.example.pms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbconnect {

    // Change the database name here
    private static final String DB_NAME = "pms";
    private static final String DB_URL = "jdbc:mysql://localhost/" + DB_NAME;
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection connection;

    private dbconnect() {
        // Private constructor to prevent instantiation
    }

    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                // Dynamically load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Establish the database connection
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            } catch (ClassNotFoundException | SQLException e) {
                // Handle the exceptions or rethrow them
                throw new RuntimeException("Failed to connect to database", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Handle the exception or log it
                e.printStackTrace();
            } finally {
                connection = null; // Reset the connection instance
            }
        }
    }
}
