package com.joblink.controller;

import com.joblink.dao.MissionDAO;
import com.joblink.dao.PropositionDAO;
import com.joblink.model.Mission;
import com.joblink.model.enums.StatutMission;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class ClientHomeController {

    @FXML
    private Label lblActiveMissions;
    @FXML
    private Label lblTotalSpent;
    @FXML
    private Label lblPendingProposals;
    @FXML
    private VBox recentMissionsBox;

    private MissionDAO missionDAO;
    private PropositionDAO propositionDAO;

    @FXML
    public void initialize() {
        missionDAO = new MissionDAO();
        propositionDAO = new PropositionDAO();
        loadDashboardData();
    }

    private void loadDashboardData() {
        new Thread(() -> {
            int clientId = LoginController.currentUser.getIdUtilisateur();
            List<Mission> allClientMissions = missionDAO.getMissionsByClient(clientId);

            long activeMissions = allClientMissions.stream()
                    .filter(m -> m.getStatut() == StatutMission.EN_COURS || m.getStatut() == StatutMission.OUVERTE)
                    .count();

            double totalSpent = allClientMissions.stream()
                    .filter(m -> m.getStatut() == StatutMission.TERMINEE)
                    .mapToDouble(Mission::getBudget)
                    .sum();

            int pendingProposalsCount = allClientMissions.stream()
                    .mapToInt(m -> (int) propositionDAO.getPropositionsByMission(m.getIdMission()).stream()
                            .filter(p -> p.getStatut() == com.joblink.model.enums.StatutProposition.EN_ATTENTE)
                            .count())
                    .sum();

            Platform.runLater(() -> {
                lblActiveMissions.setText(String.valueOf(activeMissions));
                lblTotalSpent.setText(String.format("%.0f€", totalSpent));
                lblPendingProposals.setText(String.valueOf(pendingProposalsCount));
                renderRecentMissions(allClientMissions);
            });
        }).start();
    }

    private void renderRecentMissions(List<Mission> missions) {
        recentMissionsBox.getChildren().clear();

        if (missions.isEmpty()) {
            Label empty = new Label("Aucune mission récente. Allez dans 'Créer une Mission' pour démarrer.");
            empty.getStyleClass().add("text-muted");
            recentMissionsBox.getChildren().add(empty);
            return;
        }

        missions.stream().limit(3).forEach(m -> {
            VBox card = new VBox(8);
            card.getStyleClass().add("modern-card-small");

            HBox header = new HBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label title = new Label(m.getTitre());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label badge = new Label(m.getStatut().name());
            badge.getStyleClass().add("badge");

            switch (m.getStatut()) {
                case OUVERTE -> badge.getStyleClass().add("badge-success");
                case EN_COURS -> badge.getStyleClass().add("badge-info");
                case TERMINEE -> badge.getStyleClass().add("badge-warning");
                case ANNULEE -> badge.getStyleClass().add("badge-danger");
            }

            header.getChildren().addAll(title, spacer, badge);

            Label budget = new Label("Budget: " + String.format("%.0f€", m.getBudget()));
            budget.getStyleClass().add("text-muted");

            card.getChildren().addAll(header, budget);
            recentMissionsBox.getChildren().add(card);
        });
    }
}
