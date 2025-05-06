package Controlles;

import Entities.Reclamation;
import Entities.ReclamationMessage;
import Entities.ChatMessage;
import Entites.utilisateur;
import Entites.Produit;
import Services.ReclamationService;
import Services.ReclamationMessageService;
import Services.ChatMessageService;
import Services.utilisateurService;
import Services.ServiceProduit;
import Services.TranslationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReclamationDetailsController implements Initializable {
    @FXML private Label clientIdLabel;
    @FXML private Label clientEmailLabel;
    @FXML private Label clientNameLabel;
    
    @FXML private Label productIdLabel;
    @FXML private Label productNameLabel;
    @FXML private Label productPriceLabel;
    @FXML private Label productAvailabilityLabel;
    
    @FXML private Label reclamationIdLabel;
    @FXML private Label reclamationDateLabel;
    @FXML private Label reclamationTypeLabel;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button updateStatusButton;
    @FXML private TextArea messageArea;
    @FXML private Button backButton1;

    // Chat components
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;

    @FXML private VBox chatbotMessagesContainer;

    private final ReclamationService reclamationService = new ReclamationService();
    private final ReclamationMessageService messageService = new ReclamationMessageService();
    private final ChatMessageService chatMessageService = new ChatMessageService();
    private final utilisateurService userService = new utilisateurService();
    private final ServiceProduit productService = new ServiceProduit();
    private final TranslationService translationService = new TranslationService();
    private Reclamation currentReclamation;
    private Stage stage;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private Timeline refreshTimeline;
    private int lastMessageId = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStatusComboBox();
        setupBackButton();
        setupUpdateButton();
        setupMessageRefresh();
    }

    private void setupStatusComboBox() {
        statusComboBox.setItems(FXCollections.observableArrayList("Pending", "In Progress", "Resolved"));
        statusComboBox.setValue(currentReclamation != null ? currentReclamation.getStatut_rec() : "Pending");
    }

    private void setupBackButton() {
        backButton1.setOnAction(event -> handleBackToList());
    }

    private void setupUpdateButton() {
        updateStatusButton.setOnAction(event -> handleUpdateStatus());
    }

    private void setupMessageRefresh() {
        // Rafraîchir les messages toutes les 2 secondes
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            if (currentReclamation != null) {
                refreshMessages();
            }
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    public void setReclamation(Reclamation reclamation) {
        this.currentReclamation = reclamation;
        updateDisplay();
        // Réinitialiser le lastMessageId et charger tous les messages
        lastMessageId = 0;
        messagesContainer.getChildren().clear();
        refreshMessages();
        // Charger l'historique du chatbot
        loadChatbotMessages();
    }

    private void updateDisplay() {
        if (currentReclamation != null) {
            try {
                // Afficher l'ID de la reclamation
                reclamationIdLabel.setText(String.valueOf(currentReclamation.getId()));

                // Récupérer et afficher les informations de l'utilisateur
                utilisateur user = userService.findByID(currentReclamation.getUtilisateur_id());
                if (user != null) {
                    clientIdLabel.setText(String.valueOf(user.getId_user()));
                    clientEmailLabel.setText(user.getEmail_user());
                    clientNameLabel.setText(user.getNom_user() + " " + user.getPrenom_user());
                }

                // Récupérer et afficher les informations du produit
                Produit product = productService.findById(currentReclamation.getProduit_id());
                if (product != null) {
                    productIdLabel.setText(String.valueOf(product.getId()));
                    productNameLabel.setText(product.getNomProduit());
                    productPriceLabel.setText(product.getPrixProduit() + " DT");
                    productAvailabilityLabel.setText(product.getDisponibilteProduit());
                }

                // Afficher les informations de la réclamation
                reclamationDateLabel.setText(dateFormatter.format(currentReclamation.getDate_rec()));
                reclamationTypeLabel.setText(currentReclamation.getDescription_rec());
                statusLabel.setText(currentReclamation.getStatut_rec());
                messageArea.setText(currentReclamation.getMessage_reclamation());
                statusComboBox.setValue(currentReclamation.getStatut_rec());
                
                // Ajouter un bouton de traduction
                Button translateButton = new Button("Traduire en anglais");
                translateButton.setOnAction(event -> translateMessage());
                translateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Erreur", "Impossible de charger les informations détaillées");
            }
        }
    }

    private void refreshMessages() {
        if (currentReclamation == null) {
            return;
        }
        
        try {
            List<ReclamationMessage> messages = messageService.getMessagesByReclamationId(currentReclamation.getId());
            boolean hasNewMessages = false;
            
            if (messages.isEmpty() && messagesContainer.getChildren().isEmpty()) {
                // Afficher un message indiquant qu'il n'y a pas de messages
                Label noMessagesLabel = new Label("Aucun message dans cette conversation.");
                noMessagesLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                messagesContainer.getChildren().add(noMessagesLabel);
            } else {
                for (ReclamationMessage message : messages) {
                    if (message.getId() > lastMessageId) {
                        // Check if message is already displayed by looking for a message box with the same content
                        boolean isDuplicate = false;
                        for (javafx.scene.Node node : messagesContainer.getChildren()) {
                            if (node instanceof HBox) {
                                HBox messageBox = (HBox) node;
                                VBox contentBox = (VBox) messageBox.getChildren().get(messageBox.getChildren().size() - 1);
                                Label messageLabel = (Label) contentBox.getChildren().get(0);
                                if (messageLabel.getText().equals(message.getContent())) {
                                    isDuplicate = true;
                                    break;
                                }
                            }
                        }
                        
                        if (!isDuplicate) {
                            // Supprimer le message "Aucun message" s'il existe
                            if (messagesContainer.getChildren().size() == 1 && 
                                messagesContainer.getChildren().get(0) instanceof Label) {
                                messagesContainer.getChildren().clear();
                            }
                            
                            addMessageToView(message.getContent(), message.isIs_from_admin());
                            lastMessageId = message.getId();
                            hasNewMessages = true;
                        }
                    }
                }
            }
            
            if (hasNewMessages) {
                // Use Platform.runLater to ensure UI updates happen on the JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    messagesContainer.layout();
                    // Scroll to bottom
                    messagesContainer.setLayoutY(messagesContainer.getHeight());
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de rafraîchir les messages: " + e.getMessage());
        }
    }

    @FXML
    private void sendMessage() {
        if (currentReclamation == null || messageInput.getText().trim().isEmpty()) {
            return;
        }

        String message = messageInput.getText().trim();
        ReclamationMessage recMessage = new ReclamationMessage();
        recMessage.setReclamation_id(currentReclamation.getId());
        recMessage.setSender_id(1); // Admin ID
        recMessage.setContent(message);
        recMessage.setSent_at(Timestamp.valueOf(LocalDateTime.now()));
        recMessage.setIs_from_admin(true);

        try {
            messageService.addMessage(recMessage);
            messageInput.clear();
            
            // Rafraîchir immédiatement l'affichage
            refreshMessages();
            
            // Faire défiler jusqu'au dernier message
            javafx.application.Platform.runLater(() -> {
                messagesContainer.layout();
                messagesContainer.setLayoutY(messagesContainer.getHeight());
            });
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'envoyer le message: " + e.getMessage());
        }
    }

    private void addMessageToView(String message, boolean isAdmin) {
        HBox messageBox = createMessageBox(message, isAdmin);
        messagesContainer.getChildren().add(messageBox);
    }

    private HBox createMessageBox(String message, boolean isAdmin) {
        HBox messageBox = new HBox(10);
        messageBox.setMaxWidth(1130.0); // Set to full width
        
        VBox contentBox = new VBox(5);
        contentBox.setMaxWidth(500); // Limit message bubble width
        
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
        
        if (isAdmin) {
            // Pour l'admin, ajouter un Region qui pousse le message à droite
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            messageBox.getChildren().addAll(spacer, contentBox);
            timeLabel.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageBox.getChildren().add(contentBox);
            timeLabel.setAlignment(Pos.CENTER_LEFT);
        }
        
        return messageBox;
    }

    private void handleUpdateStatus() {
        if (currentReclamation != null) {
            String newStatus = statusComboBox.getValue();
            if (newStatus != null && !newStatus.equals(currentReclamation.getStatut_rec())) {
                currentReclamation.setStatut_rec(newStatus);
                reclamationService.updateReclamation(currentReclamation);
                statusLabel.setText(newStatus);
            }
        }
    }

    @FXML
    private void handleBackToList() {
        if (backButton1 != null) {
            Stage stage = (Stage) backButton1.getScene().getWindow();
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Called when the controller is being stopped.
     * Cleans up resources and stops the refresh timeline.
     */
    public void cleanup() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }

    /**
     * Charge et affiche l'historique des messages du chatbot pour cette réclamation.
     */
    private void loadChatbotMessages() {
        chatbotMessagesContainer.getChildren().clear();
        
        if (currentReclamation != null) {
            try {
                List<ChatMessage> chatMessages = chatMessageService.getChatMessagesByReclamationId(currentReclamation.getId());
                
                if (chatMessages.isEmpty()) {
                    // Afficher un message indiquant qu'il n'y a pas de messages
                    Label noMessagesLabel = new Label("Aucun message dans cette conversation.");
                    noMessagesLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    chatbotMessagesContainer.getChildren().add(noMessagesLabel);
                } else {
                    for (ChatMessage chatMessage : chatMessages) {
                        // Afficher le message de l'utilisateur
                        if (chatMessage.getMessage() != null && !chatMessage.getMessage().isEmpty()) {
                            addChatbotMessageToView(chatMessage.getMessage(), true);
                        }
                        
                        // Afficher la réponse du chatbot
                        if (chatMessage.getResponse() != null && !chatMessage.getResponse().isEmpty()) {
                            addChatbotMessageToView(chatMessage.getResponse(), false);
                        }
                    }
                }
                
                // Faire défiler jusqu'au dernier message
                javafx.application.Platform.runLater(() -> {
                    chatbotMessagesContainer.layout();
                    chatbotMessagesContainer.setLayoutY(chatbotMessagesContainer.getHeight());
                });
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Erreur", "Impossible de charger les messages du chatbot: " + e.getMessage());
                
                // Afficher un message d'erreur dans l'interface
                Label errorLabel = new Label("Erreur lors du chargement des messages.");
                errorLabel.setStyle("-fx-text-fill: red;");
                chatbotMessagesContainer.getChildren().add(errorLabel);
            }
        }
    }

    /**
     * Ajoute un message à l'affichage de l'historique chatbot.
     * @param message Le texte du message
     * @param isUser true si c'est l'utilisateur, false si c'est le bot
     */
    private void addChatbotMessageToView(String message, boolean isUser) {
        HBox messageBox = new HBox(10);
        messageBox.setMaxWidth(1130.0);
        VBox contentBox = new VBox(5);
        contentBox.setMaxWidth(500);
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle(
            "-fx-background-color: " + (isUser ? "#007bff" : "#fff") + ";" +
            "-fx-text-fill: " + (isUser ? "white" : "#0288d1") + ";" +
            "-fx-padding: 10;" +
            "-fx-background-radius: 10;" +
            (isUser ? "" : "-fx-border-color: #b3e5fc; -fx-border-width: 1.5;")
        );
        contentBox.getChildren().add(messageLabel);
        if (isUser) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            messageBox.getChildren().addAll(spacer, contentBox);
        } else {
            messageBox.getChildren().add(contentBox);
        }
        chatbotMessagesContainer.getChildren().add(messageBox);
    }

    @FXML
    private void translateMessage() {
        if (currentReclamation != null && currentReclamation.getMessage_reclamation() != null) {
            try {
                String translatedText = translationService.translate(
                    currentReclamation.getMessage_reclamation(),
                    "en" // Langue cible (anglais)
                );
                messageArea.setText(translatedText);
            } catch (Exception e) {
                showErrorAlert("Erreur de traduction", "Impossible de traduire le message: " + e.getMessage());
            }
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 