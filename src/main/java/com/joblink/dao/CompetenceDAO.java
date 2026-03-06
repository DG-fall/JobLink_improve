package com.joblink.dao;

import com.joblink.model.Competence;
import com.joblink.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompetenceDAO implements IDAO<Competence> {

    private Connection connection;

    public CompetenceDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public Competence create(Competence competence) {
        String sql = "INSERT INTO Competence (nom) VALUES (?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, competence.getNom());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        competence.setIdCompetence(generatedKeys.getInt(1));
                        return competence;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Competence getById(int id) {
        String sql = "SELECT * FROM Competence WHERE id_competence = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCompetenceFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Competence> getAll() {
        List<Competence> competences = new ArrayList<>();
        String sql = "SELECT * FROM Competence ORDER BY nom";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                competences.add(extractCompetenceFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return competences;
    }

    @Override
    public Competence update(Competence competence) {
        String sql = "UPDATE Competence SET nom = ? WHERE id_competence = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, competence.getNom());
            pstmt.setInt(2, competence.getIdCompetence());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return competence;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Competence WHERE id_competence = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques pour les relations Many-to-Many

    public List<Competence> getCompetencesByFreelance(int idFreelance) {
        List<Competence> competences = new ArrayList<>();
        String sql = "SELECT c.* FROM Competence c " +
                     "INNER JOIN Freelance_Competence fc ON c.id_competence = fc.id_competence " +
                     "WHERE fc.id_freelance = ? ORDER BY c.nom";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idFreelance);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    competences.add(extractCompetenceFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return competences;
    }

    public List<Competence> getCompetencesByMission(int idMission) {
        List<Competence> competences = new ArrayList<>();
        String sql = "SELECT c.* FROM Competence c " +
                     "INNER JOIN Mission_Competence mc ON c.id_competence = mc.id_competence " +
                     "WHERE mc.id_mission = ? ORDER BY c.nom";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMission);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    competences.add(extractCompetenceFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return competences;
    }

    public boolean addCompetenceToFreelance(int idFreelance, int idCompetence) {
        String sql = "INSERT INTO Freelance_Competence (id_freelance, id_competence) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idFreelance);
            pstmt.setInt(2, idCompetence);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeCompetenceFromFreelance(int idFreelance, int idCompetence) {
        String sql = "DELETE FROM Freelance_Competence WHERE id_freelance = ? AND id_competence = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idFreelance);
            pstmt.setInt(2, idCompetence);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addCompetenceToMission(int idMission, int idCompetence) {
        String sql = "INSERT INTO Mission_Competence (id_mission, id_competence) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMission);
            pstmt.setInt(2, idCompetence);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeCompetenceFromMission(int idMission, int idCompetence) {
        String sql = "DELETE FROM Mission_Competence WHERE id_mission = ? AND id_competence = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMission);
            pstmt.setInt(2, idCompetence);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Méthode utilitaire pour extraire un objet Competence depuis ResultSet
    private Competence extractCompetenceFromResultSet(ResultSet rs) throws SQLException {
        Competence competence = new Competence();
        competence.setIdCompetence(rs.getInt("id_competence"));
        competence.setNom(rs.getString("nom"));
        return competence;
    }
}
