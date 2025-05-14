package Controlles;

import Entities.Reclamation;
import Services.ReclamationService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.net.URL;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class FrontReclamationController implements Initializable {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextArea messageField;
    @FXML private Button submitButton;
    @FXML private VBox formContainer;
    @FXML private Label statusLabel;
    @FXML private Label typeLabel;
    @FXML private Label messageLabel;
    @FXML private Label characterCountLabel;
    @FXML private Label messageErrorLabel;
    @FXML private Label typeErrorLabel;
    @FXML private Label productNameLabel;
    @FXML private Button loginButton;
    @FXML private Button mesReclamationsButton;
    @FXML private Button supervisionButton;
    @FXML private Button plusButton;
    @FXML private VBox userMenuBox;
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    
    private final ReclamationService reclamationService = new ReclamationService();
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MIN_MESSAGE_LENGTH = 10;
    private final BooleanProperty isSubmitting = new SimpleBooleanProperty(false);
    private int productId;
    private String productName;
    private int userId;
    private int currentUserId = 0; // Pour suivre l'état de connexion

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupTypeComboBox();
        setupTextLimits();
        setupFormValidation();
        setupErrorLabels();
        setupNavigation();
        setCurrentUserId(currentUserId); // Pour initialiser la navbar
    }

    private void setupUI() {
        // Style the form container
        formContainer.setSpacing(15);
        formContainer.setPadding(new Insets(20));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Style labels with required field indicator
        typeLabel.setText("Type de Réclamation *");
        messageLabel.setText("Message *");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        messageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Style error labels
        messageErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
        typeErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
        messageErrorLabel.setVisible(false);
        typeErrorLabel.setVisible(false);

        // Style the character count
        characterCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        // Style fields
        messageField.setWrapText(true);
        messageField.setPrefRowCount(5);
        messageField.setPromptText("Décrivez votre problème en détail (minimum " + MIN_MESSAGE_LENGTH + " caractères)");
        
        // Style the submit button
        submitButton.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-padding: 10 20;
            -fx-background-radius: 5;
            -fx-cursor: hand;
        """);
    }

    private void setupTypeComboBox() {
        typeComboBox.setItems(FXCollections.observableArrayList(
            "Problème de Livraison",
            "Produit Défectueux",
            "Produit Non Conforme",
            "Autre"
        ));
        typeComboBox.setPromptText("Sélectionnez le type de réclamation");
        
        // Add validation listener
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateType();
            updateSubmitButtonState();
        });
    }

    private void setupTextLimits() {
        messageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > MAX_MESSAGE_LENGTH) {
                messageField.setText(oldValue);
            }
            updateCharacterCount();
            validateMessage();
            updateSubmitButtonState();
        });
    }

    private void setupErrorLabels() {
        messageErrorLabel.setVisible(false);
        typeErrorLabel.setVisible(false);
    }

    private void validateType() {
        boolean isValid = typeComboBox.getValue() != null && !typeComboBox.getValue().isEmpty();
        typeErrorLabel.setVisible(!isValid);
        typeErrorLabel.setText(isValid ? "" : "Veuillez sélectionner un type de réclamation");
        typeComboBox.setStyle(isValid ? "" : "-fx-border-color: #dc3545;");
    }

    private void validateMessage() {
        String message = messageField.getText();
        boolean isValid = message != null && message.length() >= MIN_MESSAGE_LENGTH;
        messageErrorLabel.setVisible(!isValid);
        
        if (message == null || message.isEmpty()) {
            messageErrorLabel.setText("Le message est requis");
        } else if (message.length() < MIN_MESSAGE_LENGTH) {
            messageErrorLabel.setText("Le message doit contenir au moins " + MIN_MESSAGE_LENGTH + " caractères");
        }
        
        messageField.setStyle(isValid ? "" : "-fx-border-color: #dc3545;");
    }

    private void updateCharacterCount() {
        int currentLength = messageField.getText().length();
        int remainingChars = MAX_MESSAGE_LENGTH - currentLength;
        String labelText = String.format("Caractères restants: %d", remainingChars);
        characterCountLabel.setText(labelText);
        
        if (currentLength < MIN_MESSAGE_LENGTH) {
            characterCountLabel.setStyle("-fx-text-fill: #dc3545;");
        } else if (remainingChars < 100) {
            characterCountLabel.setStyle("-fx-text-fill: #ffc107;");
        } else {
            characterCountLabel.setStyle("-fx-text-fill: #666666;");
        }
    }

    private void updateSubmitButtonState() {
        boolean isValid = isFormValid();
        submitButton.setDisable(!isValid || isSubmitting.get());
        
        if (isValid) {
            submitButton.setStyle("""
                -fx-background-color: #4CAF50;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
                -fx-padding: 10 20;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """);
        } else {
            submitButton.setStyle("""
                -fx-background-color: #cccccc;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
                -fx-padding: 10 20;
                -fx-background-radius: 5;
            """);
        }
    }

    private boolean isFormValid() {
        String message = messageField.getText();
        return typeComboBox.getValue() != null && 
               !typeComboBox.getValue().isEmpty() &&
               message != null && 
               message.length() >= MIN_MESSAGE_LENGTH &&
               message.length() <= MAX_MESSAGE_LENGTH;
    }

    @FXML
    private void handleSubmit() {
        if (!isFormValid()) {
            validateType();
            validateMessage();
            return;
        }

        Reclamation reclamation = createReclamation();
        submitReclamationAsync(reclamation);
    }

    private void showValidationError() {
        StringBuilder errors = new StringBuilder();
        if (typeComboBox.getValue() == null) {
            errors.append("Please select a type of reclamation.\n");
        }
        if (messageField.getText().isEmpty()) {
            errors.append("Please enter a message.\n");
        }
        if (messageField.getText().length() > MAX_MESSAGE_LENGTH) {
            errors.append(String.format("Message cannot exceed %d characters.\n", MAX_MESSAGE_LENGTH));
        }
        
        showErrorAlert("Validation Error", errors.toString());
    }

    private Reclamation createReclamation() {
        Reclamation reclamation = new Reclamation();
        reclamation.setUtilisateur_id(currentUserId);
        reclamation.setProduit_id(productId);
        reclamation.setDescription_rec(typeComboBox.getValue());
        reclamation.setMessage_reclamation(messageField.getText());
        reclamation.setStatut_rec("Pending");
        reclamation.setDate_rec(new java.util.Date());
        
        return reclamation;
    }

    private void submitReclamationAsync(Reclamation reclamation) {
        isSubmitting.set(true);
        updateStatus("Submitting reclamation...");
        formContainer.setDisable(true);

        CompletableFuture.runAsync(() -> {
            reclamationService.addReclamation(reclamation);
        }).thenRunAsync(() -> {
            Platform.runLater(() -> {
                showSuccessAlert();
                clearForm();
                formContainer.setDisable(false);
                isSubmitting.set(false);
                updateStatus("");
                
                // Redirect to user reclamations list
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ReclamationUser.fxml"));
                    Parent root = loader.load();
                    
                    ReclamationUserController controller = loader.getController();
                    controller.setCurrentUserId(userId);
                    
                    Stage stage = (Stage) formContainer.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Mes Réclamations");
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorAlert("Erreur", "Impossible de charger la liste des réclamations");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                showErrorAlert("Erreur", "Échec de l'envoi de la réclamation: " + e.getMessage());
                formContainer.setDisable(false);
                isSubmitting.set(false);
                updateStatus("Échec de l'envoi. Veuillez réessayer.");
            });
            return null;
        });
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Votre message a été envoyé avec succès.");
        
        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: white;
            -fx-padding: 20;
        """);
        
        // Add custom button styling
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8 15;
        """);
        
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the error alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("""
            -fx-background-color: white;
            -fx-padding: 20;
        """);
        
        alert.showAndWait();
    }

    private void clearForm() {
        typeComboBox.setValue(null);
        messageField.clear();
        updateCharacterCount();
        setupErrorLabels();
        messageField.setStyle("");
        typeComboBox.setStyle("");
    }

    private void disableForm(boolean disable) {
        formContainer.setDisable(disable);
        submitButton.setDisable(disable);
    }

    private void updateStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
        });
    }

    private void setupFormValidation() {
        // Initial validation
        validateType();
        validateMessage();
        updateSubmitButtonState();
        
        // Add listeners for real-time validation
        messageField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus is lost
                validateMessage();
            }
        });
        
        typeComboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus is lost
                validateType();
            }
        });
    }

    private void setupNavigation() {
        boolean isLoggedIn = (currentUserId != 0);
        
        // Vérifier si les éléments existent avant de les utiliser
        if (loginButton != null) loginButton.setVisible(!isLoggedIn);
        if (mesReclamationsButton != null) mesReclamationsButton.setVisible(isLoggedIn);
        if (supervisionButton != null) supervisionButton.setVisible(isLoggedIn);
        if (plusButton != null) plusButton.setVisible(isLoggedIn);
        if (userMenuBox != null) userMenuBox.setVisible(false);
        
        // Mettre à jour les labels nom/prénom
        if (isLoggedIn) {
            Services.utilisateurService us = new Services.utilisateurService();
            Entites.utilisateur currentUser = us.findByID(currentUserId);
            if (currentUser != null) {
                if (nomLabel != null) nomLabel.setText(currentUser.getNom_user());
                if (prenomLabel != null) prenomLabel.setText(currentUser.getPrenom_user());
            } else {
                if (nomLabel != null) nomLabel.setText("");
                if (prenomLabel != null) prenomLabel.setText("");
            }
        } else {
            if (nomLabel != null) nomLabel.setText("");
            if (prenomLabel != null) prenomLabel.setText("");
        }
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
        if (productNameLabel != null) {
            productNameLabel.setText(productName);
        }
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @FXML
    private void accueilFront() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
            Parent root = loader.load();
            Controlles.FrontEnd controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) plusButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur de Navigation", "Impossible d'accéder à la page d'accueil");
        }
    }

    @FXML
    private void InscFront() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUtilisateur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) plusButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur de Navigation", "Impossible d'accéder à la page d'inscription");
        }
    }

    @FXML
    private void FrontProduit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
            Parent root = loader.load();
            Controlles.FrontEnd controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) plusButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Produits");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur de Navigation", "Impossible d'accéder à la page des produits");
        }
    }

    @FXML
    private void handleMesReclamations() {
        if (currentUserId == 0) {
            showLoginPrompt();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ReclamationUser.fxml"));
            Parent root = loader.load();
            Controlles.ReclamationUserController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) plusButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Réclamations");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupervision() {
        if (currentUserId == 0) {
            showLoginPrompt();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Supervision.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) plusButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Supervision");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePlusButton() {
        if (plusButton != null && userMenuBox != null) {
            userMenuBox.setVisible(!userMenuBox.isVisible());
        }
    }

    private void showLoginPrompt() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connexion requise");
        alert.setHeaderText(null);
        alert.setContentText("Veuillez vous connecter pour accéder à cette fonctionnalité.");
        alert.showAndWait();
        
        // Rediriger vers la page de connexion
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) plusButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
            Parent root = loader.load();
            Controlles.ProfileUser controller = loader.getController();
            Services.utilisateurService us = new Services.utilisateurService();
            Entites.utilisateur currentUser = us.findByID(currentUserId);
            if (currentUser != null) {
                controller.setCurrentUserId(currentUser.getId_user());
                controller.setNomfx(currentUser.getNom_user());
                controller.setPrenomfx(currentUser.getPrenom_user());
                controller.setEmailfx(currentUser.getEmail_user());
                controller.setPwdfx(currentUser.getMot_de_passe_user());
                controller.setAdressfx(currentUser.getEmail_user());
                controller.setCodefx(currentUser.getCode_postal_user());
                controller.setTelfx(currentUser.getTelephone_user());
                controller.setVillefx(currentUser.getVille_user());
            }
            Stage stage = (Stage) plusButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Profil utilisateur");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        currentUserId = 0;
        setCurrentUserId(0);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) plusButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour mettre à jour l'état de connexion et la navbar
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        this.userId = userId;
        setupNavigation();
    }
} 