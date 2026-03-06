package com.joblink.controller;

import com.joblink.dao.MissionDAO;
import com.joblink.dao.PropositionDAO;
import com.joblink.dao.UtilisateurDAO;
import com.joblink.model.Mission;
import com.joblink.model.Proposition;
import com.joblink.model.Utilisateur;
import com.joblink.model.enums.StatutMission;
import com.joblink.model.enums.StatutProposition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.stream.Collectors;

public class ClientPropositionsController {

    @FXML
    private VBox propositionsBox;
    @FXML
    private ComboBox<Mission> missionFilter;

    private MissionDAO missionDAO;
    private PropositionDAO propositionDAO;
    private UtilisateurDAO utilisateurDAO;
    private List<Mission> myMissions;

    @FXML
    public void initialize() {
        missionDAO = new MissionDAO();
        propositionDAO = new PropositionDAO();
        utilisateurDAO = new UtilisateurDAO();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            int clientId = LoginController.currentUser.getIdUtilisateur();
            myMissions = missionDAO.getMissionsByClient(clientId);

            Platform.runLater(() -> {
                missionFilter.getItems().setAll(myMissions);
                missionFilter.setCellFactory(p -> new ListCell<Mission>() {
                    @Override
                    protected void updateItem(Mission m, boolean empty) {
                        super.updateItem(m, empty);
                        setText(empty || m == null ? null : m.getTitre());
                    }
                });
                missionFilter.setButtonCell(new ListCell<Mission>() {
                    @Override
                    protected void updateItem(Mission m, boolean empty) {
                        super.updateItem(m, empty);
                        setText(empty || m == null ? "Toutes les missions" : m.getTitre());
                    }
                });
                renderAllPropositions();
            });
        }).start();
    }

    @FXML
    private void handleMissionFilter() {
        Mission selected = missionFilter.getValue();
        if (selected == null) {
            renderAllPropositions();
        } else {
            new Thread(() -> {
                List<Proposition> props = propositionDAO.getPropositionsByMission(selected.getIdMission());
                Platform.runLater(() -> renderPropositions(props, selected));
            }).start();
        }
    }

    private void renderAllPropositions() {
        propositionsBox.getChildren().clear();
        if (myMissions.isEmpty()) {
            Label empty = new Label("Aucune mission publiée.");
            empty.getStyleClass().add("text-muted");
            propositionsBox.getChildren().add(empty);
            return;
        }
        for (Mission m : myMissions) {
            new Thread(() -> {
                List<Proposition> props = propositionDAO.getPropositionsByMission(m.getIdMission());
                if (!props.isEmpty()) {
                    Platform.runLater(() -> renderPropositions(props, m));
                }
            }).start();
        }
    }

    private void renderPropositions(List<Proposition> props, Mission mission) {
        // Section header for this mission
        Label sectionLabel = new Label("📋  " + mission.getTitre() + " — " + props.size() + " candidature(s)");
        sectionLabel.setStyle(
                "-fx-font-weight: 900; -fx-font-size: 17px; -fx-text-fill: -fx-color-foreground; -fx-padding: 15 0 5 0;");
        propositionsBox.getChildren().add(sectionLabel);

        for (Proposition p : props) {
            Utilisateur freelance = utilisateurDAO.getById(p.getIdFreelance());
            String nom = freelance != null ? freelance.getPrenom() + " " + freelance.getNom()
                    : "Freelance #" + p.getIdFreelance();

            VBox card = new VBox(12);
            card.getStyleClass().add("modern-card-small");

            // Header: Freelance name + status badge
            HBox header = new HBox(12);
            header.setAlignment(Pos.CENTER_LEFT);
            Label nameLabel = new Label("👤  " + nom);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label badge = new Label(p.getStatut().name());
            badge.getStyleClass().add("badge");
            if (p.getStatut() == StatutProposition.ACCEPTEE)
                badge.getStyleClass().add("success");
            else if (p.getStatut() == StatutProposition.REFUSEE)
                badge.setStyle(
                        "-fx-background-color: -fx-color-destructive; -fx-text-fill: white; -fx-background-radius: 12px; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: bold;");
            header.getChildren().addAll(nameLabel, spacer, badge);

            // Montant + message
            Label montantLabel = new Label("💰  Devis : " + String.format("%.0f €", p.getMontantPropose()));
            montantLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -fx-color-primary;");
            Label msgLabel = new Label(p.getMessage() != null ? "\"" + p.getMessage() + "\"" : "");
            msgLabel.getStyleClass().add("text-muted");
            msgLabel.setWrapText(true);

            // Actions (only show if still pending and mission is open)
            HBox actions = new HBox(12);
            actions.setAlignment(Pos.CENTER_RIGHT);
            if (p.getStatut() == StatutProposition.EN_ATTENTE && mission.getStatut() == StatutMission.OUVERTE) {
                Button acceptBtn = new Button("✓  Accepter");
                acceptBtn.getStyleClass().addAll("button", "primary");
                acceptBtn.setOnAction(e -> handleAccept(p, mission));

                Button refuseBtn = new Button("✗  Refuser");
                refuseBtn.getStyleClass().addAll("button", "secondary");
                refuseBtn.setStyle("-fx-text-fill: -fx-color-destructive;");
                refuseBtn.setOnAction(e -> handleRefuse(p));

                actions.getChildren().addAll(refuseBtn, acceptBtn);
            }

            card.getChildren().addAll(header, montantLabel, msgLabel);
            if (!actions.getChildren().isEmpty())
                card.getChildren().add(actions);
            propositionsBox.getChildren().add(card);
        }
    }

    private void handleAccept(Proposition p, Mission mission) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Accepter cette proposition ? La mission passera en statut 'En cours'.", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmer l'acceptation");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                new Thread(() -> {
                    propositionDAO.accepterProposition(p.getIdProposition());
                    // Change mission status to EN_COURS
                    mission.setStatut(StatutMission.EN_COURS);
                    missionDAO.update(mission);
                    Platform.runLater(this::loadData);
                }).start();
            }
        });
    }

    private void handleRefuse(Proposition p) {
        new Thread(() -> {
            propositionDAO.refuserProposition(p.getIdProposition());
            Platform.runLater(this::loadData);
        }).start();
    }
}
