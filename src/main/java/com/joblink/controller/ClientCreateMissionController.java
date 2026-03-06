package com.joblink.controller;

import com.joblink.dao.CompetenceDAO;
import com.joblink.dao.MissionDAO;
import com.joblink.dao.NotificationDAO;
import com.joblink.model.Competence;
import com.joblink.model.Mission;
import com.joblink.model.enums.StatutMission;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.List;

public class ClientCreateMissionController {

    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField budgetField;
    @FXML
    private DatePicker dateLimitePicker;
    @FXML
    private DatePicker dateLivraisonPicker;
    @FXML
    private ListView<Competence> competencesListView;
    @FXML
    private TextField newCompetenceField;
    @FXML
    private Label feedbackLabel;

    private MissionDAO missionDAO;
    private CompetenceDAO competenceDAO;

    @FXML
    public void initialize() {
        missionDAO = new MissionDAO();
        competenceDAO = new CompetenceDAO();

        // Multi-selection enabled
        competencesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Custom cell representation for Competence ListView
        competencesListView.setCellFactory(param -> new ListCell<Competence>() {
            @Override
            protected void updateItem(Competence item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });

        loadCompetences();
    }

    private void loadCompetences() {
        new Thread(() -> {
            List<Competence> competences = competenceDAO.getAll();
            Platform.runLater(() -> {
                competencesListView.getItems().setAll(competences);
            });
        }).start();
    }

    @FXML
    private void handleAddNewCompetence() {
        String nom = newCompetenceField.getText().trim();
        if (nom.isEmpty())
            return;

        new Thread(() -> {
            Competence c = new Competence();
            c.setNom(nom);
            Competence created = competenceDAO.create(c);

            Platform.runLater(() -> {
                if (created != null) {
                    competencesListView.getItems().add(created);
                    competencesListView.getSelectionModel().select(created);
                    newCompetenceField.clear();
                } else {
                    showFeedback("Erreur lors de la création de la compétence", true);
                }
            });
        }).start();
    }

    @FXML
    private void handleCreate() {
        String titre = titreField.getText().trim();
        String desc = descriptionArea.getText().trim();
        String budgetStr = budgetField.getText().trim();
        LocalDate limite = dateLimitePicker.getValue();
        LocalDate livraison = dateLivraisonPicker.getValue();

        if (titre.isEmpty() || desc.isEmpty() || budgetStr.isEmpty() || limite == null) {
            showFeedback("Veuillez remplir tous les champs obligatoires (*Titre, *Description, *Budget, *Date Limite)",
                    true);
            return;
        }

        double budget;
        try {
            budget = Double.parseDouble(budgetStr.replace(",", "."));
        } catch (NumberFormatException e) {
            showFeedback("Budget invalide. Utilisez un nombre valide.", true);
            return;
        }

        if (limite.isBefore(LocalDate.now())) {
            showFeedback("La date limite ne peut pas être dans le passé.", true);
            return;
        }

        Mission mission = new Mission();
        mission.setTitre(titre);
        mission.setDescription(desc);
        mission.setBudget(budget);
        mission.setDateLimite(limite);
        mission.setDateLivraisonPrevue(livraison);
        mission.setStatut(StatutMission.OUVERTE);
        mission.setIdClient(LoginController.currentUser.getIdUtilisateur());

        // Save on background thread
        new Thread(() -> {
            Mission createdMission = missionDAO.create(mission);

            if (createdMission != null) {
                // Save related competencies
                List<Competence> selected = competencesListView.getSelectionModel().getSelectedItems();
                for (Competence c : selected) {
                    competenceDAO.addCompetenceToMission(createdMission.getIdMission(), c.getIdCompetence());
                }

                Platform.runLater(() -> {
                    NotificationDAO notifDAO = new NotificationDAO();
                    notifDAO.ajouterNotification(LoginController.currentUser.getIdUtilisateur(),
                            "Mission Publiée", "Votre mission '" + titre + "' a été publiée avec succès.");

                    showFeedback("Mission publiée avec succès !", false);
                    clearForm();
                });
            } else {
                Platform.runLater(() -> showFeedback("Erreur lors de la publication.", true));
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        clearForm();
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
    }

    private void clearForm() {
        titreField.clear();
        descriptionArea.clear();
        budgetField.clear();
        dateLimitePicker.setValue(null);
        dateLivraisonPicker.setValue(null);
        competencesListView.getSelectionModel().clearSelection();
    }

    private void showFeedback(String msg, boolean isError) {
        feedbackLabel.setText(msg);
        feedbackLabel.setStyle(isError
                ? "-fx-text-fill: -fx-color-destructive; -fx-font-weight: bold;"
                : "-fx-text-fill: -fx-color-success; -fx-font-weight: bold;");
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
    }
}
