package com.joblink.test;

import com.joblink.utils.DatabaseConnection;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("TEST DE CONNEXION À LA BASE DE DONNÉES");
        System.out.println("========================================\n");

        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            System.out.println("✅ SUCCÈS : Connexion établie !");
            System.out.println("📊 Base de données : joblink_db");
            System.out.println("🌐 Serveur : localhost:3306");
        } else {
            System.err.println("❌ ÉCHEC : Impossible de se connecter");
            System.err.println("\nVérifiez :");
            System.err.println("1. MySQL est démarré (XAMPP/WAMP)");
            System.err.println("2. La base 'joblink_db' existe");
            System.err.println("3. Utilisateur = root, Mot de passe = vide");
        }

        System.out.println("\n========================================");
    }
}
