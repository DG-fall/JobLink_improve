package com.joblink.controller;

import com.joblink.dao.MessageDAO;
import com.joblink.dao.PropositionDAO;
import com.joblink.dao.UtilisateurDAO;
import com.joblink.model.Message;
import com.joblink.model.Utilisateur;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MessagingController {

    @FXML
    private ListView<Utilisateur> contactsList;
    @FXML
    private VBox messagesBox;
    @FXML
    private TextField messageField;
    @FXML
    private Label contactNameLabel;
    @FXML
    private ScrollPane chatScroll;

    private MessageDAO messageDAO;
    private UtilisateurDAO utilisateurDAO;
    private PropositionDAO propositionDAO;
    private Utilisateur selectedContact;
    private int currentUserId;

    @FXML
    public void initialize() {
        messageDAO = new MessageDAO();
        utilisateurDAO = new UtilisateurDAO();
        propositionDAO = new PropositionDAO();
        currentUserId = LoginController.currentUser.getIdUtilisateur();

        contactsList.setCellFactory(p -> new ListCell<Utilisateur>() {
            @Override
            protected void updateItem(Utilisateur u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) {
                    setText(null);
                } else {
                    setText(u.getPrenom() + " " + u.getNom());
                }
            }
        });

        loadContacts();
    }

    private void loadContacts() {
        new Thread(() -> {
            // Get all users this user has exchanged messages with
            List<Message> myMessages = messageDAO.getMessagesByUser(currentUserId);
            Set<Integer> contactIds = new HashSet<>();
            for (Message m : myMessages) {
                if (m.getIdExpediteur() != currentUserId)
                    contactIds.add(m.getIdExpediteur());
                if (m.getIdDestinataire() != currentUserId)
                    contactIds.add(m.getIdDestinataire());
            }

            // Also add contacts from propositions (relevant connections)
            List<Message> allMessages = messageDAO.getAll();

            List<Utilisateur> contacts = contactIds.stream()
                    .map(utilisateurDAO::getById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // If no contacts yet, show a hint
            Platform.runLater(() -> {
                contactsList.getItems().setAll(contacts);
                if (contacts.isEmpty()) {
                    Label hint = new Label(
                            "Aucune conversation.\nEnvoyez un message depuis\nune proposition pour commencer.");
                    hint.getStyleClass().add("text-muted");
                    hint.setStyle("-fx-padding: 20;");
                    hint.setWrapText(true);
                    // Use a placeholder
                    contactsList.setPlaceholder(hint);
                }
            });
        }).start();
    }

    @FXML
    private void handleContactSelected() {
        selectedContact = contactsList.getSelectionModel().getSelectedItem();
        if (selectedContact == null)
            return;
        contactNameLabel.setText(selectedContact.getPrenom() + " " + selectedContact.getNom());
        loadConversation();
    }

    private void loadConversation() {
        if (selectedContact == null)
            return;
        new Thread(() -> {
            List<Message> conv = messageDAO.getConversation(currentUserId, selectedContact.getIdUtilisateur());
            Platform.runLater(() -> {
                messagesBox.getChildren().clear();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");
                for (Message m : conv) {
                    boolean isMine = m.getIdExpediteur() == currentUserId;

                    HBox wrapper = new HBox();
                    wrapper.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                    VBox bubble = new VBox(4);
                    bubble.setMaxWidth(450);
                    bubble.setPadding(new Insets(10, 14, 10, 14));
                    bubble.setStyle(isMine
                            ? "-fx-background-color: -fx-color-primary; -fx-background-radius: 18 18 4 18;"
                            : "-fx-background-color: -fx-color-card; -fx-background-radius: 18 18 18 4; -fx-border-color: -fx-color-border; -fx-border-width: 1; -fx-border-radius: 18 18 18 4;");

                    Label textLabel = new Label(m.getContenu());
                    textLabel.setWrapText(true);
                    textLabel.setStyle(isMine ? "-fx-text-fill: white; -fx-font-size: 14px;"
                            : "-fx-text-fill: -fx-color-foreground; -fx-font-size: 14px;");

                    Label timeLabel = new Label(m.getDateEnvoi() != null ? m.getDateEnvoi().format(fmt) : "");
                    timeLabel.setStyle(isMine ? "-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;"
                            : "-fx-text-fill: -fx-color-muted-foreground; -fx-font-size: 11px;");

                    bubble.getChildren().addAll(textLabel, timeLabel);
                    wrapper.getChildren().add(bubble);
                    messagesBox.getChildren().add(wrapper);
                }
                // Auto-scroll to bottom
                chatScroll.layout();
                chatScroll.setVvalue(1.0);
            });
        }).start();
    }

    @FXML
    private void sendMessage() {
        if (selectedContact == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un contact.", ButtonType.OK).showAndWait();
            return;
        }
        String content = messageField.getText().trim();
        if (content.isEmpty())
            return;

        messageField.clear();
        new Thread(() -> {
            Message msg = new Message();
            msg.setContenu(content);
            msg.setIdExpediteur(currentUserId);
            msg.setIdDestinataire(selectedContact.getIdUtilisateur());
            msg.setDateEnvoi(LocalDateTime.now());
            messageDAO.create(msg);
            loadConversation();
        }).start();
    }
}
