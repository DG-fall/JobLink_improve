package com.joblink.dao;

import com.joblink.model.Message;
import com.joblink.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO implements IDAO<Message> {

    private Connection connection;

    public MessageDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public Message create(Message message) {
        String sql = "INSERT INTO Message (contenu, date_envoi, id_expediteur, id_destinataire) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, message.getContenu());
            pstmt.setTimestamp(2, Timestamp.valueOf(message.getDateEnvoi()));
            pstmt.setInt(3, message.getIdExpediteur());
            pstmt.setInt(4, message.getIdDestinataire());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        message.setIdMessage(generatedKeys.getInt(1));
                        return message;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Message getById(int id) {
        String sql = "SELECT * FROM Message WHERE id_message = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMessageFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Message> getAll() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM Message ORDER BY date_envoi DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                messages.add(extractMessageFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public Message update(Message message) {
        String sql = "UPDATE Message SET contenu = ? WHERE id_message = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, message.getContenu());
            pstmt.setInt(2, message.getIdMessage());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return message;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Message WHERE id_message = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques

    public List<Message> getConversation(int idUser1, int idUser2) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM Message WHERE " +
                     "(id_expediteur = ? AND id_destinataire = ?) OR " +
                     "(id_expediteur = ? AND id_destinataire = ?) " +
                     "ORDER BY date_envoi ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUser1);
            pstmt.setInt(2, idUser2);
            pstmt.setInt(3, idUser2);
            pstmt.setInt(4, idUser1);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(extractMessageFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<Message> getMessagesByUser(int idUser) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM Message WHERE id_expediteur = ? OR id_destinataire = ? ORDER BY date_envoi DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUser);
            pstmt.setInt(2, idUser);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(extractMessageFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    private Message extractMessageFromResultSet(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setIdMessage(rs.getInt("id_message"));
        message.setContenu(rs.getString("contenu"));

        Timestamp dateEnvoi = rs.getTimestamp("date_envoi");
        if (dateEnvoi != null) {
            message.setDateEnvoi(dateEnvoi.toLocalDateTime());
        }

        message.setIdExpediteur(rs.getInt("id_expediteur"));
        message.setIdDestinataire(rs.getInt("id_destinataire"));

        return message;
    }
}
