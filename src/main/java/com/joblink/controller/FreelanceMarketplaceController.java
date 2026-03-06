package com.joblink.controller;

import com.joblink.dao.CompetenceDAO;
import com.joblink.dao.MissionDAO;
import com.joblink.dao.PropositionDAO;
import com.joblink.dao.NotificationDAO;
import com.joblink.model.Competence;
import com.joblink.model.Mission;
import com.joblink.model.Proposition;
import com.joblink.model.enums.StatutProposition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FreelanceMarketplaceController {

    @FXML
    private TilePane missionsGrid;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> competenceFilter;
    @FXML
    private TextField budgetMinField;
    @FXML
    private TextField budgetMaxField;
    @FXML
    private Label resultCountLabel;

    private MissionDAO missionDAO;
    private PropositionDAO propositionDAO;
    private CompetenceDAO competenceDAO;
    private List<Mission> allMissions;

    @FXML
    public void initialize() {
        missionDAO = new MissionDAO();
        propositionDAO = new PropositionDAO();
        competenceDAO = new CompetenceDAO();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            allMissions = missionDAO.getMissionsDisponibles();
            List<Competence> allComps = competenceDAO.getAll();
            Platform.runLater(() -> {
                competenceFilter.getItems().clear();
                competenceFilter.getItems().add("Toutes les compétences");
                allComps.stream().map(Competence::getNom).distinct().sorted().forEach(competenceFilter.getItems()::add);
                competenceFilter.setValue("Toutes les compétences");
                applyFilters();
            });
        }).start();
    }

    @FXML
    private void applyFilters() {
        if (allMissions == null)
            return;
        String q = searchField.getText().toLowerCase().trim();
        String comp = competenceFilter.getValue();
        double budgetMin = parseBudget(budgetMinField.getText(), 0);
        double budgetMax = parseBudget(budgetMaxField.getText(), Double.MAX_VALUE);

        List<Mission> filtered = allMissions.stream()
                .filter(m -> q.isEmpty() || m.getTitre().toLowerCase().contains(q) ||
                        (m.getDescription() != null && m.getDescription().toLowerCase().contains(q)))
                .filter(m -> m.getBudget() >= budgetMin && m.getBudget() <= budgetMax)
                .collect(Collectors.toList());

        // Competence filter: check required competences
        if (comp != null && !comp.equals("Toutes les compétences")) {
            filtered = filtered.stream()
                    .filter(m -> {
                        List<Competence> reqComps = competenceDAO.getCompetencesByMission(m.getIdMission());
                        return reqComps.stream().anyMatch(c -> c.getNom().equalsIgnoreCase(comp));
                    })
                    .collect(Collectors.toList());
        }

        renderMissions(filtered);
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        budgetMinField.clear();
        budgetMaxField.clear();
        competenceFilter.setValue("Toutes les compétences");
        applyFilters();
    }

    private double parseBudget(String text, double defaultVal) {
        try {
            return Double.parseDouble(text.replace(",", ".").trim());
        } catch (NumberFormatException | NullPointerException e) {
            return defaultVal;
        }
    }

    private void renderMissions(List<Mission> missions) {
        missionsGrid.getChildren().clear();
        resultCountLabel.setText(missions.size() + " mission(s) trouvée(s)");
        if (missions.isEmpty()) {
            Label empty = new Label("Aucune mission ne correspond à vos critères.");
            empty.getStyleClass().add("text-muted");
            missionsGrid.getChildren().add(empty);
            return;
        }

        for (Mission m : missions) {
            VBox card = new VBox(12);
            card.getStyleClass().addAll("modern-card", "modern-card-interactive");
            card.setPrefWidth(450);

            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            Label title = new Label(m.getTitre());
            title.setStyle("-fx-font-weight: 800; -fx-font-size: 17px;");
            title.setWrapText(true);
            title.setMaxWidth(300);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label budget = new Label(String.format("%.0f €", m.getBudget()));
            budget.setStyle("-fx-font-weight: 900; -fx-text-fill: -fx-color-primary; -fx-font-size: 16px;");
            header.getChildren().addAll(title, spacer, budget);

            Label desc = new Label(m.getDescription() != null ? m.getDescription() : "");
            desc.getStyleClass().add("text-muted");
            desc.setWrapText(true);
            desc.setMaxHeight(55);

            // Required skills chips
            FlowPane skills = new FlowPane();
            skills.setHgap(6);
            skills.setVgap(6);
            new Thread(() -> {
                List<Competence> reqComps = competenceDAO.getCompetencesByMission(m.getIdMission());
                Platform.runLater(() -> {
                    for (Competence c : reqComps) {
                        Label chip = new Label(c.getNom());
                        chip.getStyleClass().addAll("badge", "badge-info");
                        chip.setStyle("-fx-font-size: 10px; -fx-padding: 2 8;");
                        skills.getChildren().add(chip);
                    }
                });
            }).start();

            HBox footer = new HBox(10);
            footer.setAlignment(Pos.CENTER_LEFT);
            Label deadline = new Label(m.getDateLimite() != null ? "⏳ " + m.getDateLimite() : "");
            deadline.getStyleClass().add("text-muted");
            deadline.setStyle("-fx-font-size: 12px;");
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            Button postulerBtn = new Button("Postuler →");
            postulerBtn.getStyleClass().addAll("button", "primary");
            postulerBtn.setOnAction(e -> applyToMission(m));
            footer.getChildren().addAll(deadline, spacer2, postulerBtn);

            card.getChildren().addAll(header, desc, skills, footer);
            missionsGrid.getChildren().add(card);
        }
    }

    private void applyToMission(Mission m) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Postuler à : " + m.getTitre());
        dialog.setHeaderText("Budget estimé : " + String.format("%.0f €", m.getBudget()));
        ButtonType applyButtonType = new ButtonType("Envoyer Proposition", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        VBox content = new VBox(12);
        Label lMontant = new Label("Votre devis (€)");
        lMontant.setStyle("-fx-font-weight: bold;");
        TextField montantField = new TextField();
        montantField.setPromptText("Ex : 1500");

        Label lDelai = new Label("Délai de réalisation (jours)");
        lDelai.setStyle("-fx-font-weight: bold;");
        TextField delaiField = new TextField();
        delaiField.setPromptText("Ex : 15");

        Label lMsg = new Label("Lettre de motivation");
        lMsg.setStyle("-fx-font-weight: bold;");
        TextArea msgArea = new TextArea();
        msgArea.setPromptText("Expliquez pourquoi vous êtes le bon candidat...");
        msgArea.setPrefRowCount(4);
        msgArea.setWrapText(true);
        content.getChildren().addAll(lMontant, montantField, lDelai, delaiField, lMsg, msgArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButtonType) {
                try {
                    double montant = Double.parseDouble(montantField.getText().trim().replace(",", "."));
                    Proposition p = new Proposition();
                    p.setIdMission(m.getIdMission());
                    p.setIdFreelance(LoginController.currentUser.getIdUtilisateur());
                    p.setMontantPropose(montant);
                    p.setMessage(msgArea.getText().trim());
                    p.setDateProposition(LocalDateTime.now());
                    p.setStatut(StatutProposition.EN_ATTENTE);
                    propositionDAO.create(p);

                    NotificationDAO notifDAO = new NotificationDAO();
                    notifDAO.ajouterNotification(m.getIdClient(), "Nouvelle Proposition", "Le freelance "
                            + LoginController.currentUser.getPrenom() + " a postulé à votre mission : " + m.getTitre());
                    notifDAO.ajouterNotification(LoginController.currentUser.getIdUtilisateur(), "Candidature Envoyée",
                            "Votre proposition pour '" + m.getTitre() + "' a été transmise.");

                    new Alert(Alert.AlertType.INFORMATION, "Candidature envoyée avec succès ! ✓", ButtonType.OK)
                            .showAndWait();
                    loadData();
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Le montant doit être un chiffre valide.", ButtonType.OK)
                            .showAndWait();
                }
            }
            return null;
        });
        dialog.showAndWait();
    }
}
