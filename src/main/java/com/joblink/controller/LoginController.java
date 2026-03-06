package com.joblink.controller;

import com.joblink.JobLinkApp;
import com.joblink.dao.UtilisateurDAO;
import com.joblink.model.Client;
import com.joblink.model.Freelance;
import com.joblink.model.Utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

import java.io.IOException;

public class LoginController {

    // Shared Fields Let
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    // Register Specific Fields
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private RadioButton clientRadio;
    @FXML
    private RadioButton freelanceRadio;
    @FXML
    private VBox dynamicFieldsBox;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnRegister;
    @FXML
    private FontIcon logoIcon;

    // Dynamic Programmatic Fields
    private TextField entrepriseField;
    private TextField adresseField;
    private TextField tarifField;

    private UtilisateurDAO utilisateurDAO;
    public static Utilisateur currentUser;
    public static boolean isDarkTheme = true; // Default to Dark Theme

    @FXML
    public void initialize() {
        utilisateurDAO = new UtilisateurDAO();
        if (clientRadio != null && freelanceRadio != null) {
            ToggleGroup roleGroup = new ToggleGroup();
            clientRadio.setToggleGroup(roleGroup);
            freelanceRadio.setToggleGroup(roleGroup);
            setupDynamicFields();
            renderDynamicFields();
        }

        if (btnLogin != null) {
            FontIcon icon = new FontIcon(Feather.LOG_IN);
            icon.setIconSize(18);
            btnLogin.setGraphic(icon);
        }
        if (btnRegister != null) {
            FontIcon icon = new FontIcon(Feather.USER_PLUS);
            icon.setIconSize(18);
            System.out.println("USER_PLUS is " + Feather.USER_PLUS);
            btnRegister.setGraphic(icon);
        }
        if (logoIcon != null) {
            logoIcon.setIconCode(btnLogin != null ? Feather.BRIEFCASE : Feather.USER_PLUS);
            logoIcon.setIconSize(btnLogin != null ? 48 : 42);
        }
    }

    private void setupDynamicFields() {
        entrepriseField = new TextField();
        entrepriseField.setPromptText("Nom de l'entreprise");
        entrepriseField.getStyleClass().add("text-field");

        adresseField = new TextField();
        adresseField.setPromptText("Adresse postale");
        adresseField.getStyleClass().add("text-field");

        tarifField = new TextField();
        tarifField.setPromptText("Tarif journalier (TJM) en €");
        tarifField.getStyleClass().add("text-field");
    }

    @FXML
    private void handleRoleSwitch() {
        renderDynamicFields();
    }

    private void renderDynamicFields() {
        dynamicFieldsBox.getChildren().clear();
        if (clientRadio.isSelected()) {
            Label l1 = new Label("Nom Entreprise");
            l1.getStyleClass().add("text-muted");
            Label l2 = new Label("Adresse");
            l2.getStyleClass().add("text-muted");
            dynamicFieldsBox.getChildren().addAll(l1, entrepriseField, l2, adresseField);
        } else {
            Label l = new Label("Tarif Journalier Moyen (€)");
            l.getStyleClass().add("text-muted");
            dynamicFieldsBox.getChildren().addAll(l, tarifField);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir email et mot de passe.");
            return;
        }

        Utilisateur user = utilisateurDAO.login(email, password);
        if (user != null) {
            currentUser = user;
            loadDashboard(user);
        } else {
            showError("Email ou mot de passe incorrect.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || nom.isEmpty()) {
            showError("Veuillez remplir les champs obligatoires.");
            return;
        }

        Utilisateur newUser;
        if (clientRadio.isSelected()) {
            Client client = new Client();
            client.setRole(com.joblink.model.enums.Role.CLIENT);
            client.setNomEntreprise(entrepriseField.getText().trim());
            client.setAdresse(adresseField.getText().trim());
            newUser = client;
        } else {
            Freelance freelance = new Freelance();
            freelance.setRole(com.joblink.model.enums.Role.FREELANCE);
            try {
                double tarif = Double.parseDouble(tarifField.getText().trim());
                freelance.setTarifJournalier(tarif);
            } catch (NumberFormatException e) {
                showError("Tarif journalier invalide.");
                return;
            }
            newUser = freelance;
        }

        newUser.setNom(nom);
        newUser.setPrenom(prenom);
        newUser.setEmail(email);
        newUser.setHashMotDePasse(password);

        boolean success = utilisateurDAO.inscrire(newUser);
        if (success) {
            currentUser = utilisateurDAO.login(email, password);
            loadDashboard(currentUser);
        } else {
            showError("Erreur lors de l'inscription (Email déjà utilisé ?).");
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            JobLinkApp.setRoot("register");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            JobLinkApp.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void loadDashboard(Utilisateur user) {
        try {
            if (user instanceof Client) {
                JobLinkApp.setRoot("dashboard_client");
            } else {
                JobLinkApp.setRoot("dashboard_freelance");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur critique: " + e.getMessage());
        }
    }
}
