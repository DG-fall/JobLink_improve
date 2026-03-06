package com.joblink.controller;

import com.joblink.dao.PortfolioDAO;
import com.joblink.model.Portfolio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;

public class FreelancePortfolioController {

    @FXML
    private TilePane portfolioGrid;

    private PortfolioDAO portfolioDAO;

    @FXML
    public void initialize() {
        portfolioDAO = new PortfolioDAO();
        loadPortfolio();
    }

    private void loadPortfolio() {
        new Thread(() -> {
            int freelanceId = LoginController.currentUser.getIdUtilisateur();
            List<Portfolio> items = portfolioDAO.getPortfoliosByFreelance(freelanceId);

            Platform.runLater(() -> renderPortfolio(items));
        }).start();
    }

    private void renderPortfolio(List<Portfolio> items) {
        portfolioGrid.getChildren().clear();
        if (items.isEmpty()) {
            Label empty = new Label("Votre portfolio est vide. Ajoutez vos meilleures réalisations !");
            empty.getStyleClass().add("text-muted");
            portfolioGrid.getChildren().add(empty);
            return;
        }

        for (Portfolio p : items) {
            VBox card = new VBox(10);
            card.getStyleClass().add("modern-card-small");
            card.setPrefWidth(300);

            HBox header = new HBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label title = new Label(p.getTitre());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button deleteBtn = new Button("×");
            deleteBtn.getStyleClass().addAll("button", "icon-only");
            deleteBtn.setStyle("-fx-text-fill: -fx-color-destructive;");
            deleteBtn.setOnAction(e -> {
                portfolioDAO.delete(p.getIdPortfolio());
                loadPortfolio();
            });

            header.getChildren().addAll(title, spacer, deleteBtn);

            Label desc = new Label(p.getDescription() != null ? p.getDescription() : "");
            desc.getStyleClass().add("text-muted");
            desc.setWrapText(true);

            card.getChildren().addAll(header, desc);
            portfolioGrid.getChildren().add(card);
        }
    }

    @FXML
    private void addPortfolioItem() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une réalisation");
        
        // Apply global theme
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("root"); // Ensure root variables are available
        
        // Detect current theme from main window if possible, otherwise default to user pref
        // For simplicity, we assume we can check a static flag or just add a class if we knew the state.
        // Ideally, pass the current stage's scene's root style classes.
        // Here is a workaround to ensure it looks good:
        if (LoginController.isDarkTheme) { 
             dialogPane.getStyleClass().add("dark-theme");
        }

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        VBox content = new VBox(15);

        Label lTitre = new Label("Titre du projet");
        lTitre.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-color-foreground;");
        TextField titreField = new TextField();
        titreField.setPromptText("Ex: Application E-commerce en JavaFX");

        Label lDesc = new Label("Description");
        lDesc.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-color-foreground;");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Décrivez ce que vous avez fait...");
        descArea.setPrefRowCount(4);

        content.getChildren().addAll(lTitre, titreField, lDesc, descArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK && !titreField.getText().isEmpty()) {
                Portfolio p = new Portfolio();
                p.setTitre(titreField.getText().trim());
                p.setDescription(descArea.getText().trim());
                p.setIdFreelance(LoginController.currentUser.getIdUtilisateur());

                new Thread(() -> {
                    portfolioDAO.create(p);
                    Platform.runLater(this::loadPortfolio);
                }).start();
            }
            return null;
        });

        dialog.showAndWait();
    }
}
