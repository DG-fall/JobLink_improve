package com.joblink.dao;

import com.joblink.model.Evaluation;
import com.joblink.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvaluationDAO implements IDAO<Evaluation> {

    private Connection connection;

    public EvaluationDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public Evaluation create(Evaluation evaluation) {
        String sql = "INSERT INTO Evaluation (note, commentaire, date_evaluation, id_mission, id_client, id_freelance) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, evaluation.getNote());
            pstmt.setString(2, evaluation.getCommentaire());
            pstmt.setTimestamp(3, Timestamp.valueOf(evaluation.getDateEvaluation()));
            pstmt.setInt(4, evaluation.getIdMission());
            pstmt.setInt(5, evaluation.getIdClient());
            pstmt.setInt(6, evaluation.getIdFreelance());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        evaluation.setIdEvaluation(generatedKeys.getInt(1));
                        return evaluation;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Evaluation getById(int id) {
        String sql = "SELECT * FROM Evaluation WHERE id_evaluation = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractEvaluationFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Evaluation> getAll() {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = "SELECT * FROM Evaluation ORDER BY date_evaluation DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                evaluations.add(extractEvaluationFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evaluations;
    }

    @Override
    public Evaluation update(Evaluation evaluation) {
        String sql = "UPDATE Evaluation SET note = ?, commentaire = ? WHERE id_evaluation = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, evaluation.getNote());
            pstmt.setString(2, evaluation.getCommentaire());
            pstmt.setInt(3, evaluation.getIdEvaluation());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return evaluation;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Evaluation WHERE id_evaluation = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques pour Evaluation

    public List<Evaluation> getEvaluationsByFreelance(int idFreelance) {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = "SELECT * FROM Evaluation WHERE id_freelance = ? ORDER BY date_evaluation DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idFreelance);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    evaluations.add(extractEvaluationFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evaluations;
    }

    public List<Evaluation> getEvaluationsByClient(int idClient) {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = "SELECT * FROM Evaluation WHERE id_client = ? ORDER BY date_evaluation DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idClient);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    evaluations.add(extractEvaluationFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evaluations;
    }

    public Evaluation getEvaluationByMission(int idMission) {
        String sql = "SELECT * FROM Evaluation WHERE id_mission = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idMission);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractEvaluationFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getMoyenneNoteFreelance(int idFreelance) {
        String sql = "SELECT AVG(note) as moyenne FROM Evaluation WHERE id_freelance = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idFreelance);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("moyenne");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Méthode utilitaire pour extraire un objet Evaluation depuis ResultSet
    private Evaluation extractEvaluationFromResultSet(ResultSet rs) throws SQLException {
        Evaluation evaluation = new Evaluation();
        evaluation.setIdEvaluation(rs.getInt("id_evaluation"));
        evaluation.setNote(rs.getInt("note"));
        evaluation.setCommentaire(rs.getString("commentaire"));

        Timestamp dateEvaluation = rs.getTimestamp("date_evaluation");
        if (dateEvaluation != null) {
            evaluation.setDateEvaluation(dateEvaluation.toLocalDateTime());
        }

        evaluation.setIdMission(rs.getInt("id_mission"));
        evaluation.setIdClient(rs.getInt("id_client"));
        evaluation.setIdFreelance(rs.getInt("id_freelance"));

        return evaluation;
    }
}
