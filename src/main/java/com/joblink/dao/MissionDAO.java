package com.joblink.dao;

import com.joblink.model.Mission;
import com.joblink.model.enums.StatutMission;
import com.joblink.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MissionDAO implements IDAO<Mission> {

    private Connection connection;

    public MissionDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public Mission create(Mission mission) {
        String sql = "INSERT INTO Mission (titre, description, budget, date_limite, date_livraison_prevue, statut, id_client) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, mission.getTitre());
            pstmt.setString(2, mission.getDescription());
            pstmt.setDouble(3, mission.getBudget());
            pstmt.setDate(4, Date.valueOf(mission.getDateLimite()));
            pstmt.setDate(5, mission.getDateLivraisonPrevue() != null ? Date.valueOf(mission.getDateLivraisonPrevue()) : null);
            pstmt.setString(6, mission.getStatut().name());
            pstmt.setInt(7, mission.getIdClient());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        mission.setIdMission(generatedKeys.getInt(1));
                        return mission;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Mission getById(int id) {
        String sql = "SELECT * FROM Mission WHERE id_mission = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMissionFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Mission> getAll() {
        List<Mission> missions = new ArrayList<>();
        String sql = "SELECT * FROM Mission ORDER BY date_limite DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                missions.add(extractMissionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return missions;
    }

    @Override
    public Mission update(Mission mission) {
        String sql = "UPDATE Mission SET titre = ?, description = ?, budget = ?, date_limite = ?, date_livraison_prevue = ?, statut = ? WHERE id_mission = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, mission.getTitre());
            pstmt.setString(2, mission.getDescription());
            pstmt.setDouble(3, mission.getBudget());
            pstmt.setDate(4, Date.valueOf(mission.getDateLimite()));
            pstmt.setDate(5, mission.getDateLivraisonPrevue() != null ? Date.valueOf(mission.getDateLivraisonPrevue()) : null);
            pstmt.setString(6, mission.getStatut().name());
            pstmt.setInt(7, mission.getIdMission());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return mission;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Mission WHERE id_mission = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques pour Mission

    public List<Mission> getMissionsByClient(int idClient) {
        List<Mission> missions = new ArrayList<>();
        String sql = "SELECT * FROM Mission WHERE id_client = ? ORDER BY date_limite DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idClient);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    missions.add(extractMissionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return missions;
    }

    public List<Mission> getMissionsByStatut(StatutMission statut) {
        List<Mission> missions = new ArrayList<>();
        String sql = "SELECT * FROM Mission WHERE statut = ? ORDER BY date_limite DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, statut.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    missions.add(extractMissionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return missions;
    }

    public List<Mission> getMissionsDisponibles() {
        return getMissionsByStatut(StatutMission.OUVERTE);
    }

    // Méthode utilitaire pour extraire un objet Mission depuis ResultSet
    private Mission extractMissionFromResultSet(ResultSet rs) throws SQLException {
        Mission mission = new Mission();
        mission.setIdMission(rs.getInt("id_mission"));
        mission.setTitre(rs.getString("titre"));
        mission.setDescription(rs.getString("description"));
        mission.setBudget(rs.getDouble("budget"));

        Date dateLimite = rs.getDate("date_limite");
        if (dateLimite != null) {
            mission.setDateLimite(dateLimite.toLocalDate());
        }

        Date dateLivraison = rs.getDate("date_livraison_prevue");
        if (dateLivraison != null) {
            mission.setDateLivraisonPrevue(dateLivraison.toLocalDate());
        }

        mission.setStatut(StatutMission.valueOf(rs.getString("statut")));
        mission.setIdClient(rs.getInt("id_client"));

        return mission;
    }
}
