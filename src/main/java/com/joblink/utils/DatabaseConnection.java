package com.joblink.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // URL de connexion (assurez-vous que la base joblink_db existe)
    private static final String URL = "jdbc:mysql://localhost:3306/joblink_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Par défaut sur XAMPP/WAMP
    private static final String PASSWORD = ""; // Par défaut vide sur XAMPP/WAMP

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Erreur de connexion : " + e.getMessage());
            return null;
        }
    }

    // Pour tester la connexion rapidement
    public static void main(String[] args) {
        getConnection();
    }
}