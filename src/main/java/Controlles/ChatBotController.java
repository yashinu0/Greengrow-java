package Controlles;

import Services.OllamaChatService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;
import Entities.ChatMessage;
import Services.ChatMessageService;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ChatBotController {
    private static final Logger LOGGER = Logger.getLogger(ChatBotController.class.getName());
    
    @FXML private VBox chatArea;
    @FXML private TextField userInput;

    private final ChatMessageService chatMessageService = new ChatMessageService();
    private int reclamationId = -1;
    public void setReclamationId(int id) {
        this.reclamationId = id;
    }

    @FXML
    private void initialize() {
        // Optionnel : message d'accueil du bot
        addBotMessage("Bonjour ! Je suis votre assistant virtuel. Posez-moi vos questions.");
    }

    @FXML
    private void handleSend() {
        String userMsg = userInput.getText();
        if (userMsg == null || userMsg.isEmpty()) return;

        addUserMessage(userMsg);
        userInput.clear();

        // Appel à Ollama/Mistral (asynchrone recommandé en prod)
        String botReply = askOllama(userMsg);
        addBotMessage(botReply);

        // Sauvegarde dans la base
        if (reclamationId != -1) {
            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setReclamationId(reclamationId);
            chatMsg.setMessage(userMsg);
            chatMsg.setResponse(botReply);
            chatMsg.setCreatedAt(LocalDateTime.now());
            chatMessageService.addChatMessage(chatMsg);
        }
    }

    private String askOllama(String prompt) {
        try {
            return OllamaChatService.ask(prompt);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la communication avec le chatbot", e);
            return "Erreur lors de la communication avec le chatbot: " + e.getMessage() + 
                   "\nVeuillez vérifier que Ollama est bien installé et en cours d'exécution sur le port 11434.";
        }
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(4, 0, 4, 0));
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(320);
        label.setStyle("-fx-background-color: #00bcd4; -fx-text-fill: white; -fx-padding: 10 18; -fx-background-radius: 16; -fx-font-size: 15px; -fx-font-weight: bold;");
        messageBox.getChildren().add(label);
        chatArea.getChildren().add(messageBox);
    }

    private void addBotMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(4, 0, 4, 0));
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(320);
        label.setStyle("-fx-background-color: #fff; -fx-text-fill: #0288d1; -fx-padding: 10 18; -fx-background-radius: 16; -fx-border-color: #b3e5fc; -fx-border-width: 1.5; -fx-font-size: 15px; -fx-font-weight: bold;");
        messageBox.getChildren().add(label);
        chatArea.getChildren().add(messageBox);
    }
} 