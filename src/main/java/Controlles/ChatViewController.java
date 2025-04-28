package Controlles;

import Entities.ReclamationMessage;
import Services.ReclamationMessageService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import javafx.geometry.Insets;

public class ChatViewController {
    
    @FXML
    private VBox messagesContainer;
    
    @FXML
    private TextArea messageInput;
    
    @FXML
    private Button sendButton;

    private final ReclamationMessageService messageService = new ReclamationMessageService();
    private int reclamationId;
    private int userId;
    
    @FXML
    private void initialize() {
        // Add some welcome message
        addSystemMessage("Bienvenue dans le chat! Comment puis-je vous aider?");
    }

    public void setReclamationId(int reclamationId) {
        this.reclamationId = reclamationId;
        loadMessages();
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        var messages = messageService.getMessagesByReclamationId(reclamationId);
        for (ReclamationMessage message : messages) {
            addMessageToView(message.getContent(), message.isIs_from_admin());
        }
    }
    
    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            ReclamationMessage recMessage = new ReclamationMessage();
            recMessage.setReclamation_id(reclamationId);
            recMessage.setSender_id(userId);
            recMessage.setContent(message);
            recMessage.setSent_at(Timestamp.valueOf(LocalDateTime.now()));
            recMessage.setIs_from_admin(false);

            messageService.addMessage(recMessage);
            addUserMessage(message);
            messageInput.clear();
        }
    }
    
    private void addUserMessage(String message) {
        addMessageToView(message, false);
    }
    
    private void addSystemMessage(String message) {
        addMessageToView(message, true);
    }

    private void addMessageToView(String message, boolean isAdmin) {
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(4, 0, 4, 0));
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(320);
        if (!isAdmin) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            label.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white; -fx-padding: 10 18; -fx-background-radius: 16; -fx-font-size: 15px; -fx-font-weight: bold;");
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            label.setStyle("-fx-background-color: #fff; -fx-text-fill: #0288d1; -fx-padding: 10 18; -fx-background-radius: 16; -fx-border-color: #b3e5fc; -fx-border-width: 1.5; -fx-font-size: 15px; -fx-font-weight: bold;");
        }
        messageBox.getChildren().add(label);
        messagesContainer.getChildren().add(messageBox);
    }
} 