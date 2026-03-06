package com.joblink.controller;

import com.joblink.dao.FactureDAO;
import com.joblink.dao.MissionDAO;
import com.joblink.dao.PropositionDAO;
import com.joblink.model.Facture;
import com.joblink.model.Mission;
import com.joblink.model.Proposition;
import com.joblink.model.enums.StatutMission;
import com.joblink.model.enums.StatutProposition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientFacturesController {

    @FXML
    private TableView<Facture> facturesTable;
    @FXML
    private TableColumn<Facture, String> colNumero;
    @FXML
    private TableColumn<Facture, String> colMission;
    @FXML
    private TableColumn<Facture, String> colMontant;
    @FXML
    private TableColumn<Facture, String> colDate;
    @FXML
    private TableColumn<Facture, String> colStatut;
    @FXML
    private TableColumn<Facture, Void> colActions;
    @FXML
    private Label totalLabel;
    @FXML
    private Label lblPaidCount;
    @FXML
    private Label lblPendingCount;
    @FXML
    private Label lblTotalCount;

    private FactureDAO factureDAO;
    private MissionDAO missionDAO;
    private PropositionDAO propositionDAO;

    @FXML
    public void initialize() {
        factureDAO = new FactureDAO();
        missionDAO = new MissionDAO();
        propositionDAO = new PropositionDAO();
        setupTable();
        loadData();
    }

    private void setupTable() {
        colNumero.setCellValueFactory(
                c -> new SimpleStringProperty("FAC-" + String.format("%04d", c.getValue().getIdFacture())));
        colMission.setCellValueFactory(c -> {
            Mission m = missionDAO.getById(c.getValue().getIdMission());
            return new SimpleStringProperty(m != null ? m.getTitre() : "Mission #" + c.getValue().getIdMission());
        });
        colMontant.setCellValueFactory(
                c -> new SimpleStringProperty(String.format("%.2f €", c.getValue().getMontantTotal())));
        colDate.setCellValueFactory(c -> {
            if (c.getValue().getDateEmission() != null) {
                return new SimpleStringProperty(
                        c.getValue().getDateEmission().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new SimpleStringProperty("");
        });
        
        // Statut column with badge styling
        colStatut.setCellValueFactory(c -> new SimpleStringProperty("Payee"));
        colStatut.setCellFactory(col -> new TableCell<Facture, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().addAll("badge", "badge-success-soft");
                    badge.setStyle("-fx-padding: 4 12; -fx-background-radius: 20;");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        
        // Actions column with download button
        colActions.setCellFactory(col -> new TableCell<Facture, Void>() {
            private final Button downloadBtn = new Button();
            {
                FontIcon icon = new FontIcon("fth-download");
                icon.setIconSize(16);
                icon.setStyle("-fx-icon-color: #4F46E5;");
                downloadBtn.setGraphic(icon);
                downloadBtn.getStyleClass().addAll("button", "ghost");
                downloadBtn.setStyle("-fx-padding: 6 12;");
                downloadBtn.setTooltip(new Tooltip("Telecharger PDF"));
                downloadBtn.setOnAction(e -> {
                    Facture f = getTableView().getItems().get(getIndex());
                    downloadFacture(f);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(downloadBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }
    
    private void downloadFacture(Facture facture) {
        // TODO: Implement PDF export using PDFExportService
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export PDF");
        alert.setHeaderText("Facture FAC-" + String.format("%04d", facture.getIdFacture()));
        alert.setContentText("La fonctionnalite d'export PDF sera bientot disponible.");
        alert.showAndWait();
    }

    private void loadData() {
        new Thread(() -> {
            int clientId = LoginController.currentUser.getIdUtilisateur();
            List<Facture> factures = factureDAO.getFacturesByClient(clientId);
            double total = factures.stream().mapToDouble(Facture::getMontantTotal).sum();
            int totalCount = factures.size();
            // For now, all factures are considered "paid"
            int paidCount = totalCount;
            int pendingCount = 0;
            
            Platform.runLater(() -> {
                facturesTable.setItems(FXCollections.observableArrayList(factures));
                totalLabel.setText("Total depense : " + String.format("%.2f €", total));
                if (lblTotalCount != null) lblTotalCount.setText(String.valueOf(totalCount));
                if (lblPaidCount != null) lblPaidCount.setText(String.valueOf(paidCount));
                if (lblPendingCount != null) lblPendingCount.setText(String.valueOf(pendingCount));
            });
        }).start();
    }

    @FXML
    private void generateFacture() {
        // Show a dialog to pick a TERMINÉE mission that has no invoice yet
        new Thread(() -> {
            int clientId = LoginController.currentUser.getIdUtilisateur();
            List<Mission> missions = missionDAO.getMissionsByClient(clientId);
            List<Mission> terminées = missions.stream()
                    .filter(m -> m.getStatut() == StatutMission.TERMINEE)
                    .filter(m -> factureDAO.getFactureByMission(m.getIdMission()) == null)
                    .toList();

            Platform.runLater(() -> {
                if (terminées.isEmpty()) {
                    new Alert(Alert.AlertType.INFORMATION, "Aucune mission terminée sans facture.", ButtonType.OK)
                            .showAndWait();
                    return;
                }

                ChoiceDialog<Mission> choice = new ChoiceDialog<>(terminées.get(0), terminées);
                choice.setTitle("Générer une facture");
                choice.setHeaderText("Choisissez la mission pour laquelle générer une facture :");
                choice.getDialogPane().setContentText("Mission :");

                // Custom display for missions
                @SuppressWarnings("unchecked")
                ComboBox<Mission> combo = (ComboBox<Mission>) choice.getDialogPane().lookup(".combo-box");
                if (combo != null) {
                    combo.setCellFactory(p -> new ListCell<Mission>() {
                        @Override
                        protected void updateItem(Mission m, boolean empty) {
                            super.updateItem(m, empty);
                            setText(empty || m == null ? null
                                    : m.getTitre() + " - " + String.format("%.0f €", m.getBudget()));
                        }
                    });
                }

                choice.showAndWait().ifPresent(mission -> {
                    // Find the accepted proposal to get freelance ID
                    new Thread(() -> {
                        List<Proposition> props = propositionDAO.getPropositionsByMission(mission.getIdMission());
                        Proposition accepted = props.stream()
                                .filter(p -> p.getStatut() == StatutProposition.ACCEPTEE)
                                .findFirst().orElse(null);

                        int freelanceId = accepted != null ? accepted.getIdFreelance() : 0;
                        double montant = accepted != null ? accepted.getMontantPropose() : mission.getBudget();

                        Facture facture = new Facture();
                        facture.setIdMission(mission.getIdMission());
                        facture.setIdClient(clientId);
                        facture.setIdFreelance(freelanceId);
                        facture.setMontantTotal(montant);
                        facture.setDateEmission(LocalDateTime.now());

                        factureDAO.create(facture);
                        loadData();
                        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION,
                                "Facture générée avec succès !\n\nMontant : " + String.format("%.2f €", montant),
                                ButtonType.OK).showAndWait());
                    }).start();
                });
            });
        }).start();
    }
}
