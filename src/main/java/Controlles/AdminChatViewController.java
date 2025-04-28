package Controlles;

import Entities.Reclamation;
import Entities.ReclamationMessage;
import Services.ReclamationService;
import Services.ReclamationMessageService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminChatViewController implements Initializable {
    
    @FXML private ListView<Reclamation> reclamationListView;
    @FXML private TextField searchField;
    @FXML private Label clientNameLabel;
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    
    private final ReclamationService reclamationService = new ReclamationService();
    private final ReclamationMessageService messageService = new ReclamationMessageService();
    private Reclamation selectedReclamation;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadReclamations();
        setupSearch();
        setupReclamationSelection();
    }
    
    private void loadReclamations() {
        List<Reclamation> reclamations = reclamationService.getAllReclamations();
        reclamationListView.setItems(FXCollections.observableArrayList(reclamations));
        reclamationListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reclamation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Réclamation #%d - %s\n%s", 
                        item.getId(), 
                        item.getUser_name(),
                        item.getDescription_rec()));
                }
            }
        });
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadReclamations();
            } else {
                List<Reclamation> filteredList = reclamationService.getAllReclamations().stream()
                    .filter(r -> r.getDescription_rec().toLowerCase().contains(newValue.toLowerCase()) ||
                               r.getUser_name().toLowerCase().contains(newValue.toLowerCase()))
                    .toList();
                reclamationListView.setItems(FXCollections.observableArrayList(filteredList));
            }
        });
    }
    
    private void setupReclamationSelection() {
        reclamationListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedReclamation = newValue;
                    clientNameLabel.setText("Discussion avec " + newValue.getUser_name());
                    loadMessages(newValue.getId());
                }
            }
        );
    }
    
    private void loadMessages(int reclamationId) {
        messagesContainer.getChildren().clear();
        List<ReclamationMessage> messages = messageService.getMessagesByReclamationId(reclamationId);
        for (ReclamationMessage message : messages) {
            addMessageToView(message.getContent(), message.isIs_from_admin());
        }
    }
    
    @FXML
    private void sendMessage() {
        if (selectedReclamation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation d'abord.");
            return;
        }
        
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        ReclamationMessage recMessage = new ReclamationMessage();
        recMessage.setReclamation_id(selectedReclamation.getId());
        recMessage.setSender_id(1); // Assuming admin ID is 1, adjust as needed
        recMessage.setContent(message);
        recMessage.setSent_at(Timestamp.valueOf(LocalDateTime.now()));
        recMessage.setIs_from_admin(true);
        
        messageService.addMessage(recMessage);
        addMessageToView(message, true);
        messageInput.clear();
    }
    
    private void addMessageToView(String message, boolean isAdmin) {
        HBox messageBox = createMessageBox(message, isAdmin);
        messagesContainer.getChildren().add(messageBox);
    }
    
    private HBox createMessageBox(String message, boolean isAdmin) {
        HBox messageBox = new HBox(10);
        messageBox.setMaxWidth(400);
        
        VBox contentBox = new VBox(5);
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle(
            "-fx-background-color: " + (isAdmin ? "#28a745" : "#007bff") + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 10;" +
            "-fx-background-radius: 10;"
        );
        
        Label timeLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 10;");
        
        contentBox.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(contentBox);
        
        messageBox.setAlignment(isAdmin ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        return messageBox;
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 