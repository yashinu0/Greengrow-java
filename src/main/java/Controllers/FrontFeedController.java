package Controllers;

import Entities.Feed;
import Services.FeedService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class FrontFeedController implements Initializable {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField subjectField;
    @FXML private TextArea commentField;
    @FXML private Button submitButton;
    @FXML private Label nameErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label subjectErrorLabel;
    @FXML private Label commentErrorLabel;
    @FXML private Label characterCountLabel;
    
    private final FeedService feedService = new FeedService();
    private static final int MAX_COMMENT_LENGTH = 1000;
    private static final int MIN_COMMENT_LENGTH = 10;
    private static final String VALID_STYLE = "-fx-border-color: #28a745; -fx-border-width: 2px;";
    private static final String ERROR_STYLE = "-fx-border-color: #dc3545; -fx-border-width: 2px;";
    private static final String DEFAULT_STYLE = "-fx-border-color: #ced4da; -fx-border-width: 1px;";
    private final BooleanProperty isSubmitting = new SimpleBooleanProperty(false);
    private static final String NAME_REGEX = "^[a-zA-Z\\s]+$"; // Uniquement des lettres et espaces
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(gmail\\.com|outlook\\.com|esprit\\.tn)$";
    private static final String FORBIDDEN_WORDS = "(?i)(fuck|shit)"; // Mots interdits (insensible à la casse)
    private static final String NUMBERS_ONLY = "^[0-9\\s]+$"; // Uniquement des chiffres et espaces

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupValidation();
        setupCharacterCount();
    }

    private void setupUI() {
        // Style des champs
        nameField.setStyle(DEFAULT_STYLE);
        emailField.setStyle(DEFAULT_STYLE);
        subjectField.setStyle(DEFAULT_STYLE);
        commentField.setStyle(DEFAULT_STYLE);
        
        // Style des labels d'erreur
        nameErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
        emailErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
        subjectErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
        commentErrorLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");
        characterCountLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        // Style du bouton
        submitButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        submitButton.setOnMouseEntered(e -> submitButton.setStyle("-fx-background-color: #218838; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"));
        submitButton.setOnMouseExited(e -> submitButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"));

        // Masquer les labels d'erreur initialement
        nameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);
        subjectErrorLabel.setVisible(false);
        commentErrorLabel.setVisible(false);

        // Désactiver le bouton de soumission pendant l'envoi
        submitButton.disableProperty().bind(isSubmitting);
    }

    private void setupValidation() {
        // Validation du nom
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                nameField.setStyle(ERROR_STYLE);
                showError(nameErrorLabel, "Le nom est requis");
            } else if (!newValue.matches(NAME_REGEX)) {
                nameField.setStyle(ERROR_STYLE);
                showError(nameErrorLabel, "Le nom ne peut contenir que des lettres");
            } else {
                nameField.setStyle(VALID_STYLE);
                hideError(nameErrorLabel);
            }
        });

        // Validation de l'email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || !newValue.matches(EMAIL_REGEX)) {
                emailField.setStyle(ERROR_STYLE);
                showError(emailErrorLabel, "Email invalide. Utilisez gmail.com, outlook.com ou esprit.tn");
            } else {
                emailField.setStyle(VALID_STYLE);
                hideError(emailErrorLabel);
            }
        });

        // Validation du sujet
        subjectField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                subjectField.setStyle(ERROR_STYLE);
                showError(subjectErrorLabel, "Le sujet est requis");
            } else if (newValue.matches(NUMBERS_ONLY)) {
                subjectField.setStyle(ERROR_STYLE);
                showError(subjectErrorLabel, "Le sujet ne peut pas contenir uniquement des chiffres");
            } else if (newValue.matches(".*" + FORBIDDEN_WORDS + ".*")) {
                subjectField.setStyle(ERROR_STYLE);
                showError(subjectErrorLabel, "Le sujet contient des mots interdits");
            } else {
                subjectField.setStyle(VALID_STYLE);
                hideError(subjectErrorLabel);
            }
        });

        // Validation du commentaire
        commentField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.length() < MIN_COMMENT_LENGTH || newValue.length() > MAX_COMMENT_LENGTH) {
                commentField.setStyle(ERROR_STYLE);
                showError(commentErrorLabel, "Le commentaire doit contenir entre " + MIN_COMMENT_LENGTH + " et " + MAX_COMMENT_LENGTH + " caractères");
            } else if (newValue.matches(NUMBERS_ONLY)) {
                commentField.setStyle(ERROR_STYLE);
                showError(commentErrorLabel, "Le commentaire ne peut pas contenir uniquement des chiffres");
            } else if (newValue.matches(".*" + FORBIDDEN_WORDS + ".*")) {
                commentField.setStyle(ERROR_STYLE);
                showError(commentErrorLabel, "Le commentaire contient des mots interdits");
            } else {
                commentField.setStyle(VALID_STYLE);
                hideError(commentErrorLabel);
            }
        });
    }

    private void setupCharacterCount() {
        commentField.textProperty().addListener((obs, oldVal, newVal) -> {
            int currentLength = newVal != null ? newVal.length() : 0;
            characterCountLabel.setText(currentLength + "/" + MAX_COMMENT_LENGTH + " caractères");
            
            if (currentLength > MAX_COMMENT_LENGTH) {
                characterCountLabel.setStyle("-fx-text-fill: #dc3545;");
            } else if (currentLength >= MIN_COMMENT_LENGTH) {
                characterCountLabel.setStyle("-fx-text-fill: #28a745;");
            } else {
                characterCountLabel.setStyle("-fx-text-fill: #6c757d;");
            }
        });
    }

    private boolean validateName() {
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            nameField.setStyle(ERROR_STYLE);
            showError(nameErrorLabel, "Le nom est requis");
            return false;
        }
        if (!name.matches(NAME_REGEX)) {
            nameField.setStyle(ERROR_STYLE);
            showError(nameErrorLabel, "Le nom ne peut contenir que des lettres");
            return false;
        }
        nameField.setStyle(VALID_STYLE);
        hideError(nameErrorLabel);
        return true;
    }

    private boolean validateEmail() {
        String email = emailField.getText();
        if (email == null || !email.matches(EMAIL_REGEX)) {
            emailField.setStyle(ERROR_STYLE);
            showError(emailErrorLabel, "Email invalide. Utilisez gmail.com, outlook.com ou esprit.tn");
            return false;
        }
        emailField.setStyle(VALID_STYLE);
        hideError(emailErrorLabel);
        return true;
    }

    private boolean validateSubject() {
        String subject = subjectField.getText();
        if (subject == null || subject.trim().isEmpty()) {
            subjectField.setStyle(ERROR_STYLE);
            showError(subjectErrorLabel, "Le sujet est requis");
            return false;
        }
        if (subject.matches(NUMBERS_ONLY)) {
            subjectField.setStyle(ERROR_STYLE);
            showError(subjectErrorLabel, "Le sujet ne peut pas contenir uniquement des chiffres");
            return false;
        }
        if (subject.matches(".*" + FORBIDDEN_WORDS + ".*")) {
            subjectField.setStyle(ERROR_STYLE);
            showError(subjectErrorLabel, "Le sujet contient des mots interdits");
            return false;
        }
        subjectField.setStyle(VALID_STYLE);
        hideError(subjectErrorLabel);
        return true;
    }

    private boolean validateComment() {
        String comment = commentField.getText();
        if (comment == null || comment.length() < MIN_COMMENT_LENGTH || comment.length() > MAX_COMMENT_LENGTH) {
            commentField.setStyle(ERROR_STYLE);
            showError(commentErrorLabel, "Le commentaire doit contenir entre " + MIN_COMMENT_LENGTH + " et " + MAX_COMMENT_LENGTH + " caractères");
            return false;
        }
        if (comment.matches(NUMBERS_ONLY)) {
            commentField.setStyle(ERROR_STYLE);
            showError(commentErrorLabel, "Le commentaire ne peut pas contenir uniquement des chiffres");
            return false;
        }
        if (comment.matches(".*" + FORBIDDEN_WORDS + ".*")) {
            commentField.setStyle(ERROR_STYLE);
            showError(commentErrorLabel, "Le commentaire contient des mots interdits");
            return false;
        }
        commentField.setStyle(VALID_STYLE);
        hideError(commentErrorLabel);
        return true;
    }

    private void validateAll() {
        validateName();
        validateEmail();
        validateSubject();
        validateComment();
    }

    private boolean isFormValid() {
        return validateName() && validateEmail() && validateSubject() && validateComment();
    }

    private Feed createFeed() {
        Feed feed = new Feed();
        feed.setName_feed(nameField.getText().trim());
        feed.setEmail_feed(emailField.getText().trim());
        feed.setSubject_feed(subjectField.getText().trim());
        feed.setCommentaire_feed(commentField.getText().trim());
        return feed;
    }

    private void submitFeedAsync(Feed feed) {
        isSubmitting.set(true);
        submitButton.setText("Envoi en cours...");

        CompletableFuture.runAsync(() -> {
            feedService.addFeed(feed);
        }).thenRunAsync(() -> {
            showSuccessAlert();
            clearForm();
            isSubmitting.set(false);
            submitButton.setText("Envoyer");
        }, Platform::runLater).exceptionally(e -> {
            Platform.runLater(() -> {
                showErrorAlert(e.getMessage());
                isSubmitting.set(false);
                submitButton.setText("Envoyer");
            });
            return null;
        });
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        subjectField.clear();
        commentField.clear();
        hideError(nameErrorLabel);
        hideError(emailErrorLabel);
        hideError(subjectErrorLabel);
        hideError(commentErrorLabel);
        characterCountLabel.setText("0/" + MAX_COMMENT_LENGTH + " caractères");
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Votre feedback a été envoyé avec succès !");
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText("Une erreur est survenue lors de l'envoi : " + message);
        alert.showAndWait();
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.getStyleClass().add("visible");
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.getStyleClass().remove("visible");
    }

    @FXML
    private void handleSubmit() {
        // Valider tous les champs
        validateAll();

        // Vérifier si le formulaire est valide
        if (isFormValid()) {
            // Si tous les champs sont valides, on crée et soumet le feed
            Feed feed = createFeed();
            submitFeedAsync(feed);
        }
    }
} 