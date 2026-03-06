package com.joblink.controller;

import com.joblink.dao.MissionDAO;
import com.joblink.dao.PropositionDAO;
import com.joblink.model.Mission;
import com.joblink.model.enums.StatutMission;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientHomeController {

    // Stats Labels
    @FXML private Label lblActiveMissions;
    @FXML private Label lblTotalSpent;
    @FXML private Label lblPendingProposals;
    @FXML private Label lblCompletedMissions;
    @FXML private Label lblActiveTrend;
    @FXML private Label lblSpentTrend;
    
    // Status counts for pie chart legend
    @FXML private Label lblOpenCount;
    @FXML private Label lblInProgressCount;
    @FXML private Label lblCompletedCount;
    @FXML private Label lblCancelledCount;
    
    // Welcome
    @FXML private Label welcomeLabel;
    
    // Charts
    @FXML private BarChart<String, Number> spendingChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private PieChart missionStatusChart;
    @FXML private ComboBox<String> chartPeriodCombo;
    
    // Mission list
    @FXML private VBox recentMissionsBox;

    private MissionDAO missionDAO;
    private PropositionDAO propositionDAO;
    private DashboardClient parentController;

    @FXML
    public void initialize() {
        missionDAO = new MissionDAO();
        propositionDAO = new PropositionDAO();
        
        // Setup period combo
        if (chartPeriodCombo != null) {
            chartPeriodCombo.setItems(FXCollections.observableArrayList("3 mois", "6 mois", "12 mois"));
            chartPeriodCombo.setValue("6 mois");
            chartPeriodCombo.setOnAction(e -> updateSpendingChart());
        }
        
        // Set welcome message
        if (welcomeLabel != null && LoginController.currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + LoginController.currentUser.getPrenom() + " !");
        }
        
        loadDashboardData();
    }
    
    public void setParentController(DashboardClient parent) {
        this.parentController = parent;
    }

    private void loadDashboardData() {
        new Thread(() -> {
            int clientId = LoginController.currentUser.getIdUtilisateur();
            List<Mission> allClientMissions = missionDAO.getMissionsByClient(clientId);

            // Count by status
            long openMissions = allClientMissions.stream()
                    .filter(m -> m.getStatut() == StatutMission.OUVERTE)
                    .count();
            
            long inProgressMissions = allClientMissions.stream()
                    .filter(m -> m.getStatut() == StatutMission.EN_COURS)
                    .count();
                    
            long completedMissions = allClientMissions.stream()
                    .filter(m -> m.getStatut() == StatutMission.TERMINEE)
                    .count();
                    
            long cancelledMissions = allClientMissions.stream()
                    .filter(m -> m.getStatut() == StatutMission.ANNULEE)
                    .count();
            
            long activeMissions = openMissions + inProgressMissions;

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
                // Update stats cards
                lblActiveMissions.setText(String.valueOf(activeMissions));
                lblTotalSpent.setText(String.format("%.0f EUR", totalSpent));
                lblPendingProposals.setText(String.valueOf(pendingProposalsCount));
                
                if (lblCompletedMissions != null) {
                    lblCompletedMissions.setText(String.valueOf(completedMissions));
                }
                
                // Update pie chart legend counts
                if (lblOpenCount != null) lblOpenCount.setText(String.valueOf(openMissions));
                if (lblInProgressCount != null) lblInProgressCount.setText(String.valueOf(inProgressMissions));
                if (lblCompletedCount != null) lblCompletedCount.setText(String.valueOf(completedMissions));
                if (lblCancelledCount != null) lblCancelledCount.setText(String.valueOf(cancelledMissions));
                
                // Update charts
                updatePieChart(openMissions, inProgressMissions, completedMissions, cancelledMissions);
                updateSpendingChart();
                
                // Render recent missions
                renderRecentMissions(allClientMissions);
            });
        }).start();
    }
    
    private void updatePieChart(long open, long inProgress, long completed, long cancelled) {
        if (missionStatusChart == null) return;
        
        missionStatusChart.getData().clear();
        
        if (open > 0) {
            PieChart.Data openData = new PieChart.Data("Ouvertes", open);
            missionStatusChart.getData().add(openData);
        }
        if (inProgress > 0) {
            PieChart.Data inProgressData = new PieChart.Data("En cours", inProgress);
            missionStatusChart.getData().add(inProgressData);
        }
        if (completed > 0) {
            PieChart.Data completedData = new PieChart.Data("Terminees", completed);
            missionStatusChart.getData().add(completedData);
        }
        if (cancelled > 0) {
            PieChart.Data cancelledData = new PieChart.Data("Annulees", cancelled);
            missionStatusChart.getData().add(cancelledData);
        }
        
        // Apply colors
        int i = 0;
        for (PieChart.Data data : missionStatusChart.getData()) {
            String color;
            switch (data.getName()) {
                case "Ouvertes" -> color = "#10B981";
                case "En cours" -> color = "#3B82F6";
                case "Terminees" -> color = "#8B5CF6";
                case "Annulees" -> color = "#EF4444";
                default -> color = "#64748B";
            }
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        }
    }
    
    private void updateSpendingChart() {
        if (spendingChart == null) return;
        
        spendingChart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Depenses");
        
        // Get months based on period selection
        int months = 6;
        if (chartPeriodCombo != null && chartPeriodCombo.getValue() != null) {
            String period = chartPeriodCombo.getValue();
            if (period.contains("3")) months = 3;
            else if (period.contains("12")) months = 12;
        }
        
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.FRENCH);
        LocalDate now = LocalDate.now();
        
        // Generate sample data for demonstration
        // In real app, this would query actual spending by month
        for (int i = months - 1; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthName = month.format(monthFormatter);
            // Generate reasonable random values for demo
            double value = Math.random() * 5000 + 1000;
            series.getData().add(new XYChart.Data<>(monthName, value));
        }
        
        spendingChart.getData().add(series);
        
        // Style the bars
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: #4F46E5;");
            }
        }
    }

    private void renderRecentMissions(List<Mission> missions) {
        recentMissionsBox.getChildren().clear();

        if (missions.isEmpty()) {
            VBox emptyState = new VBox(12);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setStyle("-fx-padding: 24;");
            
            FontIcon icon = new FontIcon("fth-inbox");
            icon.setIconSize(40);
            icon.setStyle("-fx-icon-color: #94A3B8;");
            
            Label emptyLabel = new Label("Aucune mission");
            emptyLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #64748B;");
            
            Label emptyHint = new Label("Creez votre premiere mission pour commencer");
            emptyHint.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
            
            emptyState.getChildren().addAll(icon, emptyLabel, emptyHint);
            recentMissionsBox.getChildren().add(emptyState);
            return;
        }

        missions.stream().limit(4).forEach(m -> {
            HBox card = new HBox(12);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: -fx-color-background-secondary; -fx-background-radius: 12; -fx-padding: 16; -fx-cursor: hand;");
            
            // Status indicator
            StackPane statusIcon = new StackPane();
            statusIcon.setPrefSize(40, 40);
            statusIcon.setMinSize(40, 40);
            statusIcon.setMaxSize(40, 40);
            
            FontIcon icon = new FontIcon();
            icon.setIconSize(18);
            
            String bgColor, iconColor;
            switch (m.getStatut()) {
                case OUVERTE -> {
                    bgColor = "#D1FAE5";
                    iconColor = "#10B981";
                    icon.setIconLiteral("fth-circle");
                }
                case EN_COURS -> {
                    bgColor = "#DBEAFE";
                    iconColor = "#3B82F6";
                    icon.setIconLiteral("fth-play");
                }
                case TERMINEE -> {
                    bgColor = "#F3E8FF";
                    iconColor = "#8B5CF6";
                    icon.setIconLiteral("fth-check");
                }
                case ANNULEE -> {
                    bgColor = "#FEE2E2";
                    iconColor = "#EF4444";
                    icon.setIconLiteral("fth-x");
                }
                default -> {
                    bgColor = "#F1F5F9";
                    iconColor = "#64748B";
                    icon.setIconLiteral("fth-circle");
                }
            }
            
            statusIcon.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10;");
            icon.setStyle("-fx-icon-color: " + iconColor + ";");
            statusIcon.getChildren().add(icon);
            
            // Mission info
            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);
            
            Label title = new Label(m.getTitre());
            title.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: -fx-color-foreground;");
            
            Label budget = new Label(String.format("%.0f EUR", m.getBudget()));
            budget.setStyle("-fx-font-size: 13px; -fx-text-fill: -fx-color-muted-foreground;");
            
            info.getChildren().addAll(title, budget);
            
            // Badge
            Label badge = new Label(formatStatus(m.getStatut()));
            badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + iconColor + 
                          "; -fx-background-radius: 20; -fx-padding: 4 12; -fx-font-size: 11px; -fx-font-weight: 600;");

            card.getChildren().addAll(statusIcon, info, badge);
            recentMissionsBox.getChildren().add(card);
        });
    }
    
    private String formatStatus(StatutMission status) {
        return switch (status) {
            case OUVERTE -> "Ouverte";
            case EN_COURS -> "En cours";
            case TERMINEE -> "Terminee";
            case ANNULEE -> "Annulee";
        };
    }
    
    // Navigation methods
    @FXML
    private void goToCreateMission() {
        if (parentController != null) {
            parentController.showCreateMission();
        }
    }
    
    @FXML
    private void goToMissions() {
        if (parentController != null) {
            parentController.showMissions();
        }
    }
    
    @FXML
    private void goToPropositions() {
        if (parentController != null) {
            parentController.showPropositions();
        }
    }
    
    @FXML
    private void goToMarketplace() {
        if (parentController != null) {
            parentController.showMarketplace();
        }
    }
    
    @FXML
    private void goToFactures() {
        if (parentController != null) {
            parentController.showFactures();
        }
    }
}
