package com.joblink.controller;

import com.joblink.dao.MissionDAO;
import com.joblink.dao.PropositionDAO;
import com.joblink.model.Mission;
import com.joblink.model.Proposition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class FreelancePropositionsController {

    @FXML
    private TableView<Proposition> propositionsTable;
    @FXML
    private TableColumn<Proposition, String> colMission;
    @FXML
    private TableColumn<Proposition, String> colMontant;
    @FXML
    private TableColumn<Proposition, String> colDate;
    @FXML
    private TableColumn<Proposition, String> colStatut;

    private PropositionDAO propositionDAO;
    private MissionDAO missionDAO;

    @FXML
    public void initialize() {
        propositionDAO = new PropositionDAO();
        missionDAO = new MissionDAO();
        setupTable();
        loadData();
    }

    private void setupTable() {
        colMission.setCellValueFactory(cellData -> {
            Mission m = missionDAO.getById(cellData.getValue().getIdMission());
            return new SimpleStringProperty(m != null ? m.getTitre() : "Mission inconnue");
        });

        colMontant.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.format("%.0f €", cellData.getValue().getMontantPropose())));

        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateProposition() != null) {
                return new SimpleStringProperty(cellData.getValue().getDateProposition()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new SimpleStringProperty("");
        });

        colStatut.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut().name()));

        // Custom cell styling for status Badges
        colStatut.setCellFactory(column -> new TableCell<Proposition, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("ACCEPTEE")) {
                        setStyle("-fx-text-fill: -fx-color-success; -fx-font-weight: bold;");
                    } else if (item.equals("REFUSEE")) {
                        setStyle("-fx-text-fill: -fx-color-destructive; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: -fx-color-muted-foreground; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadData() {
        new Thread(() -> {
            int freelanceId = LoginController.currentUser.getIdUtilisateur();
            List<Proposition> myProps = propositionDAO.getPropositionsByFreelance(freelanceId);
            ObservableList<Proposition> data = FXCollections.observableArrayList(myProps);
            Platform.runLater(() -> propositionsTable.setItems(data));
        }).start();
    }
}
