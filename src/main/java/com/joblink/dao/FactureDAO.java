package com.joblink.dao;

import com.joblink.model.Facture;
import com.joblink.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FactureDAO implements IDAO<Facture> {

    private Connection connection;

    public FactureDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public Facture create(Facture facture) {
        String sql = "INSERT INTO Facture (montant_total, date_emission, id_mission, id_client, id_freelance) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, facture.getMontantTotal());
            pstmt.setTimestamp(2, Timestamp.valueOf(facture.getDateEmission()));
            pstmt.setInt(3, facture.getIdMission());
            pstmt.setInt(4, facture.getIdClient());
            pstmt.setInt(5, facture.getIdFreelance());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        facture.setIdFacture(generatedKeys.getInt(1));
                        return facture;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Facture getById(int id) {
        String sql = "SELECT * FROM Facture WHERE id_facture = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractFactureFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Facture> getAll() {
        List<Facture> factures = new ArrayList<>();
        String sql = "SELECT * FROM Facture ORDER BY date_emission DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                factures.add(extractFactureFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factures;
    }

    @Override
    public Facture update(Facture facture) {
        String sql = "UPDATE Facture SET montant_total = ?, date_emission = ? WHERE id_facture = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, facture.getMontantTotal());
            pstmt.setTimestamp(2, Timestamp.valueOf(facture.getDateEmission()));
            pstmt.setInt(3, facture.getIdFacture());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return facture;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Facture WHERE id_facture = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques pour Facture

    public List<Facture> getFacturesByClient(int idClient) {
        List<Facture> factures = new ArrayList<>();
        String sql = "SELECT * FROM Facture WHERE id_client = ? ORDER BY date_emission DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idClient);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    factures.add(extractFactureFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factures;
    }

    public List<Facture> getFacturesByFreelance(int idFreelance) {
        List<Facture> factures = new ArrayList<>();
        String sql = "SELECT * FROM Facture WHERE id_freelance = ? ORDER BY date_emission DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idFreelance);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    factures.add(extractFactureFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factures;
    }

    public Facture getFactureByMission(int idMission) {
        String sql = "SELECT * FROM Facture WHERE id_mission = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMission);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractFactureFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthode utilitaire pour extraire un objet Facture depuis ResultSet
    private Facture extractFactureFromResultSet(ResultSet rs) throws SQLException {
        Facture facture = new Facture();
        facture.setIdFacture(rs.getInt("id_facture"));
        facture.setMontantTotal(rs.getDouble("montant_total"));

        Timestamp dateEmission = rs.getTimestamp("date_emission");
        if (dateEmission != null) {
            facture.setDateEmission(dateEmission.toLocalDateTime());
        }

        facture.setIdMission(rs.getInt("id_mission"));
        facture.setIdClient(rs.getInt("id_client"));
        facture.setIdFreelance(rs.getInt("id_freelance"));

        return facture;
    }
}
