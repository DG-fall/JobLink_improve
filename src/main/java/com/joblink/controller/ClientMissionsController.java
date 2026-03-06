package com.joblink.controller;

import com.joblink.dao.EvaluationDAO;
import com.joblink.dao.MissionDAO;
import com.joblink.dao.PropositionDAO;
import com.joblink.dao.UtilisateurDAO;
import com.joblink.model.Evaluation;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ClientMissionsController {

    @FXML
    private VBox missionsBox;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;

    private MissionDAO missionDAO;
    private EvaluationDAO evaluationDAO;
    private PropositionDAO propositionDAO;
    private UtilisateurDAO utilisateurDAO;
    private List<Mission> allMissions;

    @FXML
    public void initialize() {
        missionDAO = new MissionDAO();
        evaluationDAO = new EvaluationDAO();
        propositionDAO = new PropositionDAO();
        utilisateurDAO = new UtilisateurDAO();
        statusFilter.getItems().addAll("Tous les statuts", "OUVERTE", "EN_COURS", "TERMINEE");
        statusFilter.setValue("Tous les statuts");
        loadMissions();
    }

    private void loadMissions() {
        new Thread(() -> {
            allMissions = missionDAO.getMissionsByClient(LoginController.currentUser.getIdUtilisateur());
            Platform.runLater(this::applyFilters);
        }).start();
    }

    @FXML
    private void handleFilter() {
        applyFilters();
    }

    private void applyFilters() {
        if (allMissions == null)
            return;
        String q = searchField.getText().toLowerCase();
        String statut = statusFilter.getValue();

        List<Mission> filtered = allMissions.stream()
                .filter(m -> {
                    boolean matchText = m.getTitre().toLowerCase().contains(q) ||
                            (m.getDescription() != null && m.getDescription().toLowerCase().contains(q));
                    boolean matchStatut = statut == null || statut.equals("Tous les statuts") ||
                            m.getStatut().name().equals(statut);
                    return matchText && matchStatut;
                })
                .collect(Collectors.toList());
        renderMissions(filtered);
    }

    private void renderMissions(List<Mission> missions) {
        missionsBox.getChildren().clear();
        if (missions.isEmpty()) {
            Label empty = new Label("Aucune mission trouvée.");
            empty.getStyleClass().add("text-muted");
            missionsBox.getChildren().add(empty);
            return;
        }

        for (Mission m : missions) {
            VBox card = new VBox(15);
            card.getStyleClass().addAll("modern-card", "modern-card-interactive");

            HBox header = new HBox(15);
            header.setAlignment(Pos.CENTER_LEFT);
            Label title = new Label(m.getTitre());
            title.setStyle("-fx-font-weight: 800; -fx-font-size: 18px;");
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

            Label desc = new Label(m.getDescription() != null ? m.getDescription() : "");
            desc.getStyleClass().add("text-muted");
            desc.setWrapText(true);

            HBox info = new HBox(25);
            info.setAlignment(Pos.CENTER_LEFT);
            Label budget = new Label("💰 " + String.format("%.0f €", m.getBudget()));
            budget.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label deadline = new Label(m.getDateLimite() != null ? "📅 Deadline: " + m.getDateLimite() : "");
            deadline.getStyleClass().add("text-muted");
            Label livraison = new Label(
                    m.getDateLivraisonPrevue() != null ? "🚀 Livraison: " + m.getDateLivraisonPrevue() : "");
            livraison.getStyleClass().add("text-muted");
            info.getChildren().addAll(budget, deadline, livraison);

            HBox footer = new HBox(12);
            footer.setAlignment(Pos.CENTER_RIGHT);

            // Only show Edit/Delete for OPEN missions
            if (m.getStatut() == StatutMission.OUVERTE) {
                Button editBtn = new Button("✏ Modifier");
                editBtn.getStyleClass().addAll("button", "secondary");
                editBtn.setOnAction(e -> showEditDialog(m));

                Button deleteBtn = new Button("🗑 Supprimer");
                deleteBtn.getStyleClass().addAll("button", "secondary");
                deleteBtn.setStyle("-fx-text-fill: -fx-color-destructive;");
                deleteBtn.setOnAction(e -> handleDelete(m));

                footer.getChildren().addAll(editBtn, deleteBtn);
            }

            // Mark as complete button if EN_COURS
            if (m.getStatut() == StatutMission.EN_COURS) {
                Button finishBtn = new Button("✓ Marquer Terminée");
                finishBtn.getStyleClass().addAll("button", "primary");
                finishBtn.setOnAction(e -> handleMarkFinished(m));
                footer.getChildren().add(finishBtn);
            }

            // Rate freelance button if TERMINEE and no evaluation yet
            if (m.getStatut() == StatutMission.TERMINEE) {
                Evaluation existingEval = evaluationDAO.getEvaluationByMission(m.getIdMission());
                if (existingEval == null) {
                    Button rateBtn = new Button("⭐ Évaluer le Freelance");
                    rateBtn.getStyleClass().addAll("button", "primary");
                    rateBtn.setOnAction(e -> showEvaluationDialog(m));
                    footer.getChildren().add(rateBtn);
                } else {
                    Label rated = new Label("⭐ Vous avez noté : " + existingEval.getNote() + "/5");
                    rated.setStyle("-fx-text-fill: -fx-color-muted-foreground; -fx-font-style: italic;");
                    footer.getChildren().add(rated);
                }
            }

            card.getChildren().addAll(header, desc, info);
            if (!footer.getChildren().isEmpty())
                card.getChildren().add(footer);
            missionsBox.getChildren().add(card);
        }
    }

    private void showEditDialog(Mission m) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la mission");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(15);
        Label lTitre = new Label("Titre");
        lTitre.setStyle("-fx-font-weight: bold;");
        TextField titreField = new TextField(m.getTitre());
        Label lDesc = new Label("Description");
        lDesc.setStyle("-fx-font-weight: bold;");
        TextArea descArea = new TextArea(m.getDescription());
        descArea.setPrefRowCount(4);
        descArea.setWrapText(true);
        Label lBudget = new Label("Budget (€)");
        lBudget.setStyle("-fx-font-weight: bold;");
        TextField budgetField = new TextField(String.valueOf((int) m.getBudget()));

        content.getChildren().addAll(lTitre, titreField, lDesc, descArea, lBudget, budgetField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                m.setTitre(titreField.getText().trim());
                m.setDescription(descArea.getText().trim());
                try {
                    m.setBudget(Double.parseDouble(budgetField.getText().trim()));
                } catch (NumberFormatException ignored) {
                }
                new Thread(() -> {
                    missionDAO.update(m);
                    loadMissions();
                }).start();
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void handleDelete(Mission m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la mission \"" + m.getTitre() + "\" ? Cette action est irréversible.", ButtonType.YES,
                ButtonType.NO);
        confirm.setTitle("Confirmer la suppression");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                new Thread(() -> {
                    missionDAO.delete(m.getIdMission());
                    loadMissions();
                }).start();
            }
        });
    }

    private void handleMarkFinished(Mission m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Marquer la mission \"" + m.getTitre() + "\" comme TERMINÉE ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                new Thread(() -> {
                    m.setStatut(StatutMission.TERMINEE);
                    missionDAO.update(m);
                    loadMissions();
                }).start();
            }
        });
    }

    private void showEvaluationDialog(Mission m) {
        // Find the accepted freelance for this mission
        new Thread(() -> {
            List<Proposition> props = propositionDAO.getPropositionsByMission(m.getIdMission());
            Proposition accepted = props.stream()
                    .filter(p -> p.getStatut() == StatutProposition.ACCEPTEE)
                    .findFirst().orElse(null);
            if (accepted == null) {
                Platform.runLater(() -> new Alert(Alert.AlertType.WARNING,
                        "Aucun freelance accepté pour cette mission.", ButtonType.OK).showAndWait());
                return;
            }
            Utilisateur freelance = utilisateurDAO.getById(accepted.getIdFreelance());
            String freelanceName = freelance != null ? freelance.getPrenom() + " " + freelance.getNom() : "Freelance";

            Platform.runLater(() -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Évaluer " + freelanceName);
                dialog.setHeaderText("Mission : " + m.getTitre());
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                VBox content = new VBox(12);
                Label lNote = new Label("Note (1 à 5)");
                lNote.setStyle("-fx-font-weight: bold;");
                Spinner<Integer> noteSpinner = new Spinner<>(1, 5, 5);
                noteSpinner.setEditable(false);
                Label lComment = new Label("Commentaire");
                lComment.setStyle("-fx-font-weight: bold;");
                TextArea commentArea = new TextArea();
                commentArea.setPromptText("Partager votre expérience...");
                commentArea.setPrefRowCount(3);
                commentArea.setWrapText(true);
                content.getChildren().addAll(lNote, noteSpinner, lComment, commentArea);
                dialog.getDialogPane().setContent(content);

                dialog.setResultConverter(btn -> {
                    if (btn == ButtonType.OK) {
                        Evaluation eval = new Evaluation();
                        eval.setIdMission(m.getIdMission());
                        eval.setIdClient(LoginController.currentUser.getIdUtilisateur());
                        eval.setIdFreelance(accepted.getIdFreelance());
                        eval.setNote(noteSpinner.getValue());
                        eval.setCommentaire(commentArea.getText().trim());
                        eval.setDateEvaluation(LocalDateTime.now());
                        new Thread(() -> {
                            evaluationDAO.create(eval);
                            loadMissions();
                            Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION,
                                    "Évaluation envoyée ! Merci pour votre retour.", ButtonType.OK).showAndWait());
                        }).start();
                    }
                    return null;
                });
                dialog.showAndWait();
            });
        }).start();
    }
}
