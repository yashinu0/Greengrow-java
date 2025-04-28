package Controlles;

import Entities.ReclamationMessage;
import Services.ReclamationMessageService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class ReclamationChatController implements Initializable {
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private Label titleLabel;

    private ReclamationMessageService messageService;
    private int reclamationId;
    private int userId;
    private boolean isAdmin;
    private Timer refreshTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageService = new ReclamationMessageService();
        setupMessageInput();
        setupAutoRefresh();
    }

    public void setReclamationInfo(int reclamationId, int userId, boolean isAdmin) {
        this.reclamationId = reclamationId;
        this.userId = userId;
        this.isAdmin = isAdmin;
        titleLabel.setText("Discussion de la réclamation #" + reclamationId);
        loadMessages();
    }

    private void setupMessageInput() {
        // Activer/désactiver le bouton d'envoi en fonction du contenu
        messageInput.textProperty().addListener((observable, oldValue, newValue) -> {
            sendButton.setDisable(newValue.trim().isEmpty());
        });

        // Permettre l'envoi avec Entrée (Shift+Entrée pour nouvelle ligne)
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                if (!event.isShiftDown()) {
                    event.consume();
                    handleSend();
                }
            }
        });

        sendButton.setDisable(true);
    }

    private void setupAutoRefresh() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> loadMessages());
            }
        }, 0, 5000); // Rafraîchir toutes les 5 secondes
    }

    @FXML
    private void handleSend() {
        String content = messageInput.getText().trim();
        if (!content.isEmpty()) {
            ReclamationMessage message = new ReclamationMessage(
                reclamationId,
                userId,
                content,
                isAdmin
            );
            
            messageService.addMessage(message);
            messageInput.clear();
            loadMessages();
        }
    }

    private void loadMessages() {
        List<ReclamationMessage> messages = messageService.getMessagesByReclamationId(reclamationId);
        messagesContainer.getChildren().clear();
        
        for (ReclamationMessage message : messages) {
            addMessageBubble(message);
        }
    }

    private void addMessageBubble(ReclamationMessage message) {
        VBox messageBox = new VBox(5);
        messageBox.setMaxWidth(400);

        // Créer la bulle de message
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("message-bubble");
        
        // Ajouter la classe CSS appropriée selon l'expéditeur
        if (message.isIs_from_admin() == isAdmin) {
            contentLabel.getStyleClass().add("sent");
            messageBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            contentLabel.getStyleClass().add("received");
            messageBox.setAlignment(Pos.CENTER_LEFT);
        }

        // Ajouter l'horodatage
        Label timestampLabel = new Label(message.getSent_at().toString());
        timestampLabel.getStyleClass().add("message-timestamp");
        
        messageBox.getChildren().addAll(contentLabel, timestampLabel);
        messagesContainer.getChildren().add(messageBox);
    }

    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }
} 