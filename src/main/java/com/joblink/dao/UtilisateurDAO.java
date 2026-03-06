package com.joblink.dao;

import com.joblink.model.Client;
import com.joblink.model.Freelance;
import com.joblink.model.Utilisateur;
import com.joblink.model.enums.Role;
import com.joblink.utils.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    private Connection connection;

    public UtilisateurDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public Utilisateur getById(int id) {
        String sql = "SELECT role FROM Utilisateur WHERE id_utilisateur = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Role role = Role.valueOf(rs.getString("role"));
                    return getFullUserById(id, role);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 1. INSCRIPTION (Create)
    // On reçoit un objet Utilisateur (Client ou Freelance) et on l'insère dans les
    // bonnes tables
    public boolean inscrire(Utilisateur user) {
        String sqlUser = "INSERT INTO Utilisateur (nom, prenom, email, hashMotDePasse, role) VALUES (?, ?, ?, ?, ?)";
        String sqlClient = "INSERT INTO Client (id_client, nom_entreprise, adresse) VALUES (?, ?, ?)";
        String sqlFreelance = "INSERT INTO Freelance (id_freelance, tarif_journalier) VALUES (?, ?)";

        try {
            // Début de la transaction (tout passe ou rien ne passe)
            connection.setAutoCommit(false);

            // A. Insertion dans la table parente Utilisateur
            try (PreparedStatement pstmt = connection.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, user.getNom());
                pstmt.setString(2, user.getPrenom());
                pstmt.setString(3, user.getEmail());

                // HACHAGE DU MOT DE PASSE (BCrypt)
                String hashedPassword = BCrypt.hashpw(user.getHashMotDePasse(), BCrypt.gensalt());
                pstmt.setString(4, hashedPassword);

                pstmt.setString(5, user.getRole().name());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                // Récupérer l'ID généré
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setIdUtilisateur(generatedKeys.getInt(1));
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            // B. Insertion dans la table fille (Client ou Freelance)
            if (user instanceof Client) {
                Client client = (Client) user;
                try (PreparedStatement pstmt = connection.prepareStatement(sqlClient)) {
                    pstmt.setInt(1, user.getIdUtilisateur());
                    pstmt.setString(2, client.getNomEntreprise());
                    pstmt.setString(3, client.getAdresse());
                    pstmt.executeUpdate();
                }
            } else if (user instanceof Freelance) {
                Freelance freelance = (Freelance) user;
                try (PreparedStatement pstmt = connection.prepareStatement(sqlFreelance)) {
                    pstmt.setInt(1, user.getIdUtilisateur());
                    pstmt.setDouble(2, freelance.getTarifJournalier());
                    pstmt.executeUpdate();
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 2. CONNEXION (Login)
    // Retourne un objet Client ou Freelance si succès, null sinon
    public Utilisateur login(String email, String passwordClair) {
        String sql = "SELECT * FROM Utilisateur WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("hashMotDePasse");

                    // Vérification du mot de passe avec BCrypt
                    if (BCrypt.checkpw(passwordClair, hashedPassword)) {
                        Role role = Role.valueOf(rs.getString("role"));
                        int id = rs.getInt("id_utilisateur");

                        // On reconstruit l'objet complet (Client ou Freelance)
                        return getFullUserById(id, role);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Méthode privée pour récupérer les détails spécifiques (Client/Freelance)
    private Utilisateur getFullUserById(int id, Role role) {
        String sqlClient = "SELECT * FROM Client WHERE id_client = ?";
        String sqlFreelance = "SELECT * FROM Freelance WHERE id_freelance = ?";
        String sqlBase = "SELECT * FROM Utilisateur WHERE id_utilisateur = ?";

        try (PreparedStatement pstmtBase = connection.prepareStatement(sqlBase)) {
            pstmtBase.setInt(1, id);
            try (ResultSet rsBase = pstmtBase.executeQuery()) {
                if (rsBase.next()) {
                    if (role == Role.CLIENT) {
                        try (PreparedStatement pstmtC = connection.prepareStatement(sqlClient)) {
                            pstmtC.setInt(1, id);
                            try (ResultSet rsC = pstmtC.executeQuery()) {
                                if (rsC.next()) {
                                    Client c = new Client();
                                    c.setIdUtilisateur(id);
                                    c.setNom(rsBase.getString("nom"));
                                    c.setPrenom(rsBase.getString("prenom"));
                                    c.setEmail(rsBase.getString("email"));
                                    c.setRole(Role.CLIENT);
                                    c.setNomEntreprise(rsC.getString("nom_entreprise"));
                                    c.setAdresse(rsC.getString("adresse"));
                                    return c;
                                }
                            }
                        }
                    } else {
                        try (PreparedStatement pstmtF = connection.prepareStatement(sqlFreelance)) {
                            pstmtF.setInt(1, id);
                            try (ResultSet rsF = pstmtF.executeQuery()) {
                                if (rsF.next()) {
                                    Freelance f = new Freelance();
                                    f.setIdUtilisateur(id);
                                    f.setNom(rsBase.getString("nom"));
                                    f.setPrenom(rsBase.getString("prenom"));
                                    f.setEmail(rsBase.getString("email"));
                                    f.setRole(Role.FREELANCE);
                                    f.setTarifJournalier(rsF.getDouble("tarif_journalier"));
                                    return f;
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(Utilisateur user) {
        String sql = "UPDATE Utilisateur SET nom = ?, prenom = ?, email = ? WHERE id_utilisateur = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setInt(4, user.getIdUtilisateur());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                if (user instanceof Client) {
                    String sqlC = "UPDATE Client SET nom_entreprise = ?, adresse = ? WHERE id_client = ?";
                    try (PreparedStatement pstmtC = connection.prepareStatement(sqlC)) {
                        pstmtC.setString(1, ((Client) user).getNomEntreprise());
                        pstmtC.setString(2, ((Client) user).getAdresse());
                        pstmtC.setInt(3, user.getIdUtilisateur());
                        pstmtC.executeUpdate();
                    }
                } else if (user instanceof Freelance) {
                    String sqlF = "UPDATE Freelance SET tarif_journalier = ? WHERE id_freelance = ?";
                    try (PreparedStatement pstmtF = connection.prepareStatement(sqlF)) {
                        pstmtF.setDouble(1, ((Freelance) user).getTarifJournalier());
                        pstmtF.setInt(2, user.getIdUtilisateur());
                        pstmtF.executeUpdate();
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Utilisateur> getAllUsersExcluding(int idToExclude) {
        List<Utilisateur> users = new ArrayList<>();
        String sql = "SELECT * FROM Utilisateur WHERE id_utilisateur != ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idToExclude);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Role role = Role.valueOf(rs.getString("role"));
                    Utilisateur u;
                    if (role == Role.CLIENT) {
                        u = new Client();
                    } else {
                        u = new Freelance();
                    }
                    u.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    u.setNom(rs.getString("nom"));
                    u.setPrenom(rs.getString("prenom"));
                    u.setEmail(rs.getString("email"));
                    u.setRole(role);
                    users.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}