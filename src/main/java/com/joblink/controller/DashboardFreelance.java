package com.joblink.controller;

import com.joblink.JobLinkApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.geometry.Point2D;
import javafx.stage.Window;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

import com.joblink.model.Notification;
import com.joblink.dao.NotificationDAO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DashboardFreelance {

    @FXML
    private Label userNameLabel;
    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnHome;
    @FXML
    private Button btnMarketplace;
    @FXML
    private Button btnPropositions;
    @FXML
    private Button btnPortfolio;
    @FXML
    private Button btnRevenus;
    @FXML
    private Button btnMessages;
    @FXML
    private Button btnProfileFreelance;
    @FXML
    private Button btnLogout;
    @FXML
    private Button btnThemeToggle;
    @FXML
    private Button btnNotifications;
    @FXML
    private FontIcon logoIcon;
    @FXML
    private Button btnSidebarToggle;
    @FXML
    private VBox sidebar;

    private boolean sidebarCollapsed = false;

    private List<Button> allNavButtons;

    @FXML
    public void initialize() {
        if (LoginController.currentUser != null) {
            userNameLabel.setText(LoginController.currentUser.getPrenom() + " " + LoginController.currentUser.getNom());
        }
        allNavButtons = Arrays.asList(btnHome, btnMarketplace, btnPropositions, btnPortfolio, btnRevenus, btnMessages,
                btnProfileFreelance);

        // Icons are now set in FXML to prevent disorder and loading issues

        updateNotificationBadge();
        showHome();
    }

    @FXML
    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        if (sidebarCollapsed) {
            sidebar.setPrefWidth(70);
            sidebar.setMinWidth(70);
            allNavButtons.forEach(b -> b.setText(""));
        } else {
            sidebar.setPrefWidth(260);
            sidebar.setMinWidth(260);
            restoreSidebarLabels();
        }
    }
    
    private void restoreSidebarLabels() {
        if (btnHome != null) btnHome.setText("Tableau de bord");
        if (btnMarketplace != null) btnMarketplace.setText("Marketplace");
        if (btnPropositions != null) btnPropositions.setText("Mes Propositions");
        if (btnPortfolio != null) btnPortfolio.setText("Mon Portfolio");
        if (btnRevenus != null) btnRevenus.setText("Mes Revenus");
        if (btnMessages != null) btnMessages.setText("Messages");
        if (btnProfileFreelance != null) btnProfileFreelance.setText("Mon Profil");
    }
    
    @FXML
    private void showHome() {
        setActiveButton(btnHome);
        loadView("freelance_home.fxml");
    }

    private void updateNotificationBadge() {
        if (LoginController.currentUser != null && btnNotifications != null) {
            NotificationDAO notifDAO = new NotificationDAO();
            int unread = notifDAO.getNombreNonLues(LoginController.currentUser.getIdUtilisateur());
            if (unread > 0) {
                if (btnNotifications.getGraphic() != null) {
                    btnNotifications.getGraphic().setStyle("-fx-icon-color: -fx-color-destructive;");
                }
            } else {
                if (btnNotifications.getGraphic() != null) {
                    btnNotifications.getGraphic().setStyle(""); // Default
                }
            }
        }
    }

    private void setActiveButton(Button activeBtn) {
        allNavButtons.forEach(b -> b.getStyleClass().remove("active"));
        activeBtn.getStyleClass().add("active");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(JobLinkApp.class.getResource("/fxml/" + fxmlFile));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            contentArea.getChildren().setAll(new Label("Erreur chargement: " + fxmlFile));
        }
    }

    @FXML
    private void showMarketplace() {
        setActiveButton(btnMarketplace);
        loadView("freelance_marketplace.fxml");
    }

    @FXML
    private void showPropositions() {
        setActiveButton(btnPropositions);
        loadView("freelance_propositions.fxml");
    }

    @FXML
    private void showPortfolio() {
        setActiveButton(btnPortfolio);
        loadView("freelance_portfolio.fxml");
    }

    @FXML
    private void showRevenus() {
        setActiveButton(btnRevenus);
        loadView("freelance_revenus.fxml");
    }

    @FXML
    private void showMessages() {
        setActiveButton(btnMessages);
        loadView("messaging.fxml");
    }

    @FXML
    private void showProfile() {
        setActiveButton(btnProfileFreelance);
        loadView("freelance_profile.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LoginController.currentUser = null;
        try {
            JobLinkApp.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleTheme() {
        JobLinkApp.toggleTheme();
    }

    @FXML
    private void showNotifications() {
        if (LoginController.currentUser == null)
            return;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupContent = new VBox(10);
        popupContent.getStyleClass().add("modern-card");
        popupContent.setStyle(
                "-fx-padding: 15; -fx-background-color: -fx-color-card; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-min-width: 300; -fx-max-height: 400; -fx-border-color: -fx-color-border; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label titleLabel = new Label("Vos Notifications");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -fx-color-primary;");
        popupContent.getChildren().add(titleLabel);

        NotificationDAO notifDAO = new NotificationDAO();
        java.util.List<Notification> notifs = notifDAO.getNotifications(LoginController.currentUser.getIdUtilisateur());

        if (notifs.isEmpty()) {
            Label empty = new Label("Aucune notification.");
            empty.setStyle("-fx-text-fill: -fx-color-muted-foreground;");
            popupContent.getChildren().add(empty);
        } else {
            VBox listVBox = new VBox(5);
            for (Notification n : notifs) {
                VBox item = new VBox(3);
                item.setStyle(n.isLue()
                        ? "-fx-padding: 8; -fx-border-color: transparent transparent -fx-color-border transparent; -fx-border-width: 1;"
                        : "-fx-padding: 8; -fx-background-color: -fx-color-muted; -fx-border-color: transparent transparent -fx-color-border transparent; -fx-border-width: 1;");
                Label nTitle = new Label(n.getTitre());
                nTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                Label nMsg = new Label(n.getMessage());
                nMsg.setWrapText(true);
                nMsg.setStyle("-fx-font-size: 11px;");
                item.getChildren().addAll(nTitle, nMsg);

                if (!n.isLue()) {
                    item.setOnMouseClicked(e -> {
                        notifDAO.marquerCommeLue(n.getId());
                        item.setStyle(
                                "-fx-padding: 8; -fx-border-color: transparent transparent #eaeaea transparent; -fx-border-width: 1;");
                        updateNotificationBadge();
                    });
                }
                listVBox.getChildren().add(item);
            }
            ScrollPane scroll = new ScrollPane(listVBox);
            scroll.setFitToWidth(true);
            scroll.setStyle(
                    "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
            scroll.setMaxHeight(300);
            popupContent.getChildren().add(scroll);
        }

        popup.getContent().add(popupContent);
        Window window = btnNotifications.getScene().getWindow();
        Point2D location = btnNotifications.localToScene(0, btnNotifications.getHeight());
        popup.show(window, window.getX() + location.getX() - 250, window.getY() + location.getY() + 10);
    }
}
