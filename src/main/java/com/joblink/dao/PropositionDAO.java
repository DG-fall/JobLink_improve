package com.joblink.dao;

import com.joblink.model.Proposition;
import com.joblink.model.enums.StatutProposition;
import com.joblink.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PropositionDAO implements IDAO<Proposition> {

    private Connection connection;

    public PropositionDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public Proposition create(Proposition proposition) {
        String sql = "INSERT INTO Proposition (montant_propose, message, date_proposition, statut, id_mission, id_freelance) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDouble(1, proposition.getMontantPropose());
            pstmt.setString(2, proposition.getMessage());
            pstmt.setTimestamp(3, Timestamp.valueOf(proposition.getDateProposition()));
            pstmt.setString(4, proposition.getStatut().name());
            pstmt.setInt(5, proposition.getIdMission());
            pstmt.setInt(6, proposition.getIdFreelance());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        proposition.setIdProposition(generatedKeys.getInt(1));
                        return proposition;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Proposition getById(int id) {
        String sql = "SELECT * FROM Proposition WHERE id_proposition = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractPropositionFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Proposition> getAll() {
        List<Proposition> propositions = new ArrayList<>();
        String sql = "SELECT * FROM Proposition ORDER BY date_proposition DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                propositions.add(extractPropositionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return propositions;
    }

    @Override
    public Proposition update(Proposition proposition) {
        String sql = "UPDATE Proposition SET montant_propose = ?, message = ?, statut = ? WHERE id_proposition = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, proposition.getMontantPropose());
            pstmt.setString(2, proposition.getMessage());
            pstmt.setString(3, proposition.getStatut().name());
            pstmt.setInt(4, proposition.getIdProposition());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return proposition;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Proposition WHERE id_proposition = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques pour Proposition

    public List<Proposition> getPropositionsByMission(int idMission) {
        List<Proposition> propositions = new ArrayList<>();
        String sql = "SELECT * FROM Proposition WHERE id_mission = ? ORDER BY date_proposition DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMission);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    propositions.add(extractPropositionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return propositions;
    }

    public List<Proposition> getPropositionsByFreelance(int idFreelance) {
        List<Proposition> propositions = new ArrayList<>();
        String sql = "SELECT * FROM Proposition WHERE id_freelance = ? ORDER BY date_proposition DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idFreelance);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    propositions.add(extractPropositionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return propositions;
    }

    public List<Proposition> getPropositionsByStatut(StatutProposition statut) {
        List<Proposition> propositions = new ArrayList<>();
        String sql = "SELECT * FROM Proposition WHERE statut = ? ORDER BY date_proposition DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, statut.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    propositions.add(extractPropositionFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return propositions;
    }

    public boolean accepterProposition(int idProposition) {
        String sql = "UPDATE Proposition SET statut = ? WHERE id_proposition = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, StatutProposition.ACCEPTEE.name());
            pstmt.setInt(2, idProposition);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean refuserProposition(int idProposition) {
        String sql = "UPDATE Proposition SET statut = ? WHERE id_proposition = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, StatutProposition.REFUSEE.name());
            pstmt.setInt(2, idProposition);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Méthode utilitaire pour extraire un objet Proposition depuis ResultSet
    private Proposition extractPropositionFromResultSet(ResultSet rs) throws SQLException {
        Proposition proposition = new Proposition();
        proposition.setIdProposition(rs.getInt("id_proposition"));
        proposition.setMontantPropose(rs.getDouble("montant_propose"));
        proposition.setMessage(rs.getString("message"));

        Timestamp dateProposition = rs.getTimestamp("date_proposition");
        if (dateProposition != null) {
            proposition.setDateProposition(dateProposition.toLocalDateTime());
        }

        proposition.setStatut(StatutProposition.valueOf(rs.getString("statut")));
        proposition.setIdMission(rs.getInt("id_mission"));
        proposition.setIdFreelance(rs.getInt("id_freelance"));

        return proposition;
    }
}
