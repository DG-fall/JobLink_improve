package com.joblink.controller;

import com.joblink.dao.MissionDAO;
import com.joblink.dao.PropositionDAO;
import com.joblink.dao.FactureDAO;
import com.joblink.model.Mission;
import com.joblink.model.Proposition;
import com.joblink.model.enums.StatutProposition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class FreelanceHomeController {

    @FXML private Label welcomeLabel;
    @FXML private Label lblTotalRevenus;
    @FXML private Label lblRevenusTrend;
    @FXML private Label lblPropositions;
    @FXML private Label lblMissionsEnCours;
    @FXML private Label lblNoteMoyenne;
    @FXML private Label lblAvisCount;
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private VBox recentMissionsBox;
    @FXML private VBox skillsProgressBox;

    private MissionDAO missionDAO;
    private PropositionDAO propositionDAO;
    private FactureDAO factureDAO;

    @FXML
    public void initialize() {
        missionDAO = new MissionDAO();
        propositionDAO = new PropositionDAO();
        factureDAO = new FactureDAO();
        
        if (LoginController.currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + LoginController.currentUser.getPrenom() + " !");
        }
        
        loadStats();
        loadRevenueChart();
        loadRecentMissions();
        loadSkillsProgress();
    }

    private void loadStats() {
        new Thread(() -> {
            int freelanceId = LoginController.currentUser.getIdUtilisateur();
            
            // Get propositions
            List<Proposition> propositions = propositionDAO.getPropositionsByFreelance(freelanceId);
            long pendingCount = propositions.stream()
                    .filter(p -> p.getStatut() == StatutProposition.EN_ATTENTE)
                    .count();
            long acceptedCount = propositions.stream()
                    .filter(p -> p.getStatut() == StatutProposition.ACCEPTEE)
                    .count();
            
            // Calculate total revenue from accepted propositions
            double totalRevenus = propositions.stream()
                    .filter(p -> p.getStatut() == StatutProposition.ACCEPTEE)
                    .mapToDouble(Proposition::getMontantPropose)
                    .sum();
            
            Platform.runLater(() -> {
                lblTotalRevenus.setText(String.format("%.0f EUR", totalRevenus));
                lblPropositions.setText(String.valueOf(propositions.size()));
                lblMissionsEnCours.setText(String.valueOf(acceptedCount));
                lblNoteMoyenne.setText("4.8/5");
                lblAvisCount.setText("12 avis");
            });
        }).start();
    }

    private void loadRevenueChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenus");
        
        // Generate sample data for last 6 months
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM", Locale.FRENCH);
        LocalDate now = LocalDate.now();
        
        double[] sampleData = {1200, 1800, 1500, 2200, 1900, 2500};
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            series.getData().add(new XYChart.Data<>(month.format(formatter), sampleData[5-i]));
        }
        
        revenueChart.getData().add(series);
    }

    private void loadRecentMissions() {
        recentMissionsBox.getChildren().clear();
        
        new Thread(() -> {
            List<Mission> missions = missionDAO.getAll();
            List<Mission> recent = missions.stream()
                    .filter(m -> m.getStatut() != null)
                    .limit(4)
                    .toList();
            
            Platform.runLater(() -> {
                for (Mission mission : recent) {
                    HBox card = createMissionCard(mission);
                    recentMissionsBox.getChildren().add(card);
                }
                
                if (recent.isEmpty()) {
                    Label empty = new Label("Aucune mission disponible pour le moment");
                    empty.setStyle("-fx-text-fill: -fx-color-muted-foreground;");
                    recentMissionsBox.getChildren().add(empty);
                }
            });
        }).start();
    }

    private HBox createMissionCard(Mission mission) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: -fx-color-background-secondary; -fx-background-radius: 12; -fx-padding: 12 16;");
        
        // Icon
        FontIcon icon = new FontIcon("fth-briefcase");
        icon.setIconSize(20);
        icon.setStyle("-fx-icon-color: -fx-color-primary;");
        
        // Info
        VBox info = new VBox(2);
        Label title = new Label(mission.getTitre());
        title.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");
        Label budget = new Label(String.format("%.0f EUR", mission.getBudget()));
        budget.setStyle("-fx-text-fill: -fx-color-success; -fx-font-size: 13px;");
        info.getChildren().addAll(title, budget);
        
        Region spacer = new Region();
        spacer.setMinWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        card.getChildren().addAll(icon, info, spacer);
        return card;
    }

    private void loadSkillsProgress() {
        skillsProgressBox.getChildren().clear();
        
        // Sample skills with progress
        String[][] skills = {
            {"Developpement Web", "85"},
            {"Design UI/UX", "72"},
            {"Backend Java", "90"},
            {"Base de donnees", "78"}
        };
        
        for (String[] skill : skills) {
            VBox skillBox = new VBox(6);
            
            HBox header = new HBox();
            Label name = new Label(skill[0]);
            name.setStyle("-fx-font-size: 13px;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            Label percent = new Label(skill[1] + "%");
            percent.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: -fx-color-primary;");
            header.getChildren().addAll(name, spacer, percent);
            
            ProgressBar progress = new ProgressBar(Double.parseDouble(skill[1]) / 100.0);
            progress.setMaxWidth(Double.MAX_VALUE);
            progress.setPrefHeight(8);
            
            skillBox.getChildren().addAll(header, progress);
            skillsProgressBox.getChildren().add(skillBox);
        }
    }

    @FXML
    private void goToMarketplace() {
        // Will be handled by parent controller navigation
    }

    @FXML
    private void goToPropositions() {
        // Will be handled by parent controller navigation
    }

    @FXML
    private void goToPortfolio() {
        // Will be handled by parent controller navigation
    }

    @FXML
    private void goToRevenus() {
        // Will be handled by parent controller navigation
    }
}
