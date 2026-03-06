package com.joblink.dao;

import com.joblink.model.Notification;
import com.joblink.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public NotificationDAO() {
        creerTableSiInexistante();
    }

    private void creerTableSiInexistante() {
        String query = "CREATE TABLE IF NOT EXISTS Notification (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "id_utilisateur INT NOT NULL, " +
                "titre VARCHAR(255) NOT NULL, " +
                "message TEXT NOT NULL, " +
                "lue BOOLEAN DEFAULT FALSE, " +
                "date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE CASCADE" +
                ")";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            System.err.println("Erreur creation table Notification: " + e.getMessage());
        }
    }

    public boolean ajouterNotification(int idUtilisateur, String titre, String message) {
        String query = "INSERT INTO Notification (id_utilisateur, titre, message) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUtilisateur);
            pstmt.setString(2, titre);
            pstmt.setString(3, message);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Notification> getNotifications(int idUtilisateur) {
        List<Notification> list = new ArrayList<>();
        String query = "SELECT * FROM Notification WHERE id_utilisateur = ? ORDER BY date_creation DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUtilisateur);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setIdUtilisateur(rs.getInt("id_utilisateur"));
                n.setTitre(rs.getString("titre"));
                n.setMessage(rs.getString("message"));
                n.setLue(rs.getBoolean("lue"));
                n.setDateCreation(rs.getTimestamp("date_creation"));
                list.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean marquerCommeLue(int idNotification) {
        String query = "UPDATE Notification SET lue = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idNotification);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getNombreNonLues(int idUtilisateur) {
        String query = "SELECT COUNT(*) FROM Notification WHERE id_utilisateur = ? AND lue = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idUtilisateur);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
