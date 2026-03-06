package com.joblink.controller;

import com.joblink.dao.UtilisateurDAO;
import com.joblink.model.Client;
import com.joblink.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ClientProfileController {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField entrepriseField;
    @FXML
    private TextField adresseField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label feedbackLabel;

    private UtilisateurDAO utilisateurDAO;

    @FXML
    public void initialize() {
        utilisateurDAO = new UtilisateurDAO();
        loadProfileData();
    }

    private void loadProfileData() {
        Utilisateur user = LoginController.currentUser;
        if (user != null) {
            nomField.setText(user.getNom());
            prenomField.setText(user.getPrenom() != null ? user.getPrenom() : "");
            emailField.setText(user.getEmail());

            if (user instanceof Client c) {
                entrepriseField.setText(c.getNomEntreprise() != null ? c.getNomEntreprise() : "");
                adresseField.setText(c.getAdresse() != null ? c.getAdresse() : "");
            }
        }
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
            showFeedback("Le nom et l'email sont obligatoires.", true);
            return;
        }

        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);

        if (user instanceof Client c) {
            c.setNomEntreprise(entrepriseField.getText().trim());
            c.setAdresse(adresseField.getText().trim());
        }

        if (!passwordField.getText().isEmpty()) {
            user.setHashMotDePasse(passwordField.getText());
        }

        boolean ok = utilisateurDAO.update(user);
        if (ok) {
            showFeedback("Profil Entreprise mis à jour avec succès.", false);
        } else {
            showFeedback("Erreur lors de la mise à jour.", true);
        }
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(isError ? "-fx-text-fill: -fx-color-destructive; -fx-font-weight: bold;"
                : "-fx-text-fill: -fx-color-success; -fx-font-weight: bold;");
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }
}
