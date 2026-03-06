package com.joblink.controller;

import com.joblink.dao.EvaluationDAO;
import com.joblink.dao.FactureDAO;
import com.joblink.dao.MissionDAO;
import com.joblink.model.Facture;
import com.joblink.model.Mission;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class FreelanceRevenusController {

    @FXML
    private Label lblTotalRevenus;
    @FXML
    private Label lblMissionsTerminees;
    @FXML
    private Label lblNoteMoyenne;
    @FXML
    private TableView<Facture> facturesTable;
    @FXML
    private TableColumn<Facture, String> colMission;
    @FXML
    private TableColumn<Facture, String> colMontant;
    @FXML
    private TableColumn<Facture, String> colDate;
    @FXML
    private TableColumn<Facture, String> colStatut;

    private FactureDAO factureDAO;
    private MissionDAO missionDAO;
    private EvaluationDAO evaluationDAO;

    @FXML
    public void initialize() {
        factureDAO = new FactureDAO();
        missionDAO = new MissionDAO();
        evaluationDAO = new EvaluationDAO();
        setupTable();
        loadData();
    }

    private void setupTable() {
        colMission.setCellValueFactory(c -> {
            Mission m = missionDAO.getById(c.getValue().getIdMission());
            return new SimpleStringProperty(m != null ? m.getTitre() : "Mission #" + c.getValue().getIdMission());
        });
        colMontant.setCellValueFactory(
                c -> new SimpleStringProperty(String.format("%.2f €", c.getValue().getMontantTotal())));
        colDate.setCellValueFactory(c -> {
            if (c.getValue().getDateEmission() != null) {
                return new SimpleStringProperty(
                        c.getValue().getDateEmission().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new SimpleStringProperty("");
        });
        colStatut.setCellValueFactory(c -> new SimpleStringProperty("Payée"));
        colStatut.setCellFactory(col -> new TableCell<Facture, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: -fx-color-success; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void loadData() {
        new Thread(() -> {
            int freelanceId = LoginController.currentUser.getIdUtilisateur();
            List<Facture> factures = factureDAO.getFacturesByFreelance(freelanceId);
            double total = factures.stream().mapToDouble(Facture::getMontantTotal).sum();
            double moyenne = evaluationDAO.getMoyenneNoteFreelance(freelanceId);
            long terminées = factures.size(); // Each facture = 1 completed mission payment

            Platform.runLater(() -> {
                facturesTable.setItems(FXCollections.observableArrayList(factures));
                lblTotalRevenus.setText(String.format("%.0f €", total));
                lblMissionsTerminees.setText(String.valueOf(terminées));
                lblNoteMoyenne.setText(moyenne > 0 ? String.format("%.1f / 5", moyenne) : "— / 5");
            });
        }).start();
    }
}
