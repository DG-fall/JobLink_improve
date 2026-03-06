package com.joblink.controller;

import com.joblink.dao.CompetenceDAO;
import com.joblink.dao.EvaluationDAO;
import com.joblink.dao.MissionDAO;
import com.joblink.dao.UtilisateurDAO;
import com.joblink.model.Competence;
import com.joblink.model.Evaluation;
import com.joblink.model.Freelance;
import com.joblink.model.Utilisateur;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class FreelanceProfileController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField tarifField;
    @FXML
    private FlowPane competencesBox;
    @FXML
    private TextField newCompetenceField;
    @FXML
    private Label feedbackLabel;
    @FXML
    private Label lblMoyenne;
    @FXML
    private Label lblNbAvis;
    @FXML
    private VBox evaluationsBox;

    private UtilisateurDAO utilisateurDAO;
    private CompetenceDAO competenceDAO;
    private EvaluationDAO evaluationDAO;
    private MissionDAO missionDAO;

    @FXML
    public void initialize() {
        utilisateurDAO = new UtilisateurDAO();
        competenceDAO = new CompetenceDAO();
        evaluationDAO = new EvaluationDAO();
        missionDAO = new MissionDAO();
        loadProfileData();
        loadEvaluations();
    }

    private void loadProfileData() {
        Utilisateur user = LoginController.currentUser;
        if (user != null) {
            nomField.setText(user.getNom());
            prenomField.setText(user.getPrenom() != null ? user.getPrenom() : "");
            emailField.setText(user.getEmail());
            if (user instanceof Freelance f) {
                tarifField.setText(String.valueOf(f.getTarifJournalier()));
                loadCompetences(user.getIdUtilisateur());
            }
        }
    }

    private void loadCompetences(int freelanceId) {
        new Thread(() -> {
            List<Competence> comps = competenceDAO.getCompetencesByFreelance(freelanceId);
            Platform.runLater(() -> {
                competencesBox.getChildren().clear();
                for (Competence c : comps) {
                    Label chip = new Label(c.getNom() + "  ×");
                    chip.setStyle(
                            "-fx-background-color: -fx-color-accent; -fx-text-fill: -fx-color-primary; -fx-background-radius: 15px; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-weight: bold;");
                    chip.setOnMouseClicked(e -> {
                        competenceDAO.removeCompetenceFromFreelance(freelanceId, c.getIdCompetence());
                        loadCompetences(freelanceId);
                    });
                    competencesBox.getChildren().add(chip);
                }
                if (comps.isEmpty()) {
                    Label empty = new Label("Aucune compétence ajoutée.");
                    empty.getStyleClass().add("text-muted");
                    competencesBox.getChildren().add(empty);
                }
            });
        }).start();
    }

    private void loadEvaluations() {
        new Thread(() -> {
            int freelanceId = LoginController.currentUser.getIdUtilisateur();
            List<Evaluation> evals = evaluationDAO.getEvaluationsByFreelance(freelanceId);
            double moyenne = evaluationDAO.getMoyenneNoteFreelance(freelanceId);

            Platform.runLater(() -> {
                lblMoyenne.setText(moyenne > 0 ? String.format("%.1f / 5 ⭐", moyenne) : "— / 5");
                lblNbAvis.setText(String.valueOf(evals.size()));
                evaluationsBox.getChildren().clear();

                if (evals.isEmpty()) {
                    Label empty = new Label("Aucune évaluation reçue pour l'instant.");
                    empty.getStyleClass().add("text-muted");
                    evaluationsBox.getChildren().add(empty);
                    return;
                }

                for (Evaluation e : evals) {
                    VBox card = new VBox(8);
                    card.getStyleClass().add("modern-card-small");

                    HBox header = new HBox(10);
                    header.setAlignment(Pos.CENTER_LEFT);
                    String stars = "⭐".repeat(e.getNote()) + "☆".repeat(5 - e.getNote());
                    Label starsLabel = new Label(stars);
                    starsLabel.setStyle("-fx-font-size: 18px;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Utilisateur client = utilisateurDAO.getById(e.getIdClient());
                    String clientName = client != null ? client.getPrenom() + " " + client.getNom() : "Client";
                    Label clientLabel = new Label("par " + clientName);
                    clientLabel.getStyleClass().add("text-muted");
                    header.getChildren().addAll(starsLabel, spacer, clientLabel);

                    Label comment = new Label(e.getCommentaire() != null ? "\"" + e.getCommentaire() + "\"" : "");
                    comment.setStyle("-fx-font-style: italic;");
                    comment.setWrapText(true);
                    comment.getStyleClass().add("text-muted");

                    card.getChildren().addAll(header, comment);
                    evaluationsBox.getChildren().add(card);
                }
            });
        }).start();
    }

    @FXML
    private void addCompetence() {
        String nom = newCompetenceField.getText().trim();
        if (nom.isEmpty())
            return;
        new Thread(() -> {
            Competence c = new Competence();
            c.setNom(nom);
            Competence created = competenceDAO.create(c);
            if (created != null && LoginController.currentUser != null) {
                competenceDAO.addCompetenceToFreelance(LoginController.currentUser.getIdUtilisateur(),
                        created.getIdCompetence());
                Platform.runLater(() -> {
                    newCompetenceField.clear();
                    loadCompetences(LoginController.currentUser.getIdUtilisateur());
                });
            }
        }).start();
    }

    @FXML
    private void saveProfile() {
        Utilisateur user = LoginController.currentUser;
        if (user == null)
            return;
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        if (nom.isEmpty() || email.isEmpty()) {
            showFeedback("Nom et email obligatoires.", true);
            return;
        }
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        if (user instanceof Freelance f) {
            try {
                f.setTarifJournalier(Double.parseDouble(tarifField.getText().replace(",", ".")));
            } catch (NumberFormatException e) {
                showFeedback("Tarif invalide.", true);
                return;
            }
        }
        if (!passwordField.getText().isEmpty())
            user.setHashMotDePasse(passwordField.getText());
        boolean ok = utilisateurDAO.update(user);
        showFeedback(ok ? "Profil mis à jour !" : "Erreur lors de la mise à jour.", !ok);
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(isError ? "-fx-text-fill: -fx-color-destructive; -fx-font-weight: bold;"
                : "-fx-text-fill: -fx-color-success; -fx-font-weight: bold;");
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }
}
