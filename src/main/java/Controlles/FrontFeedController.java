package Controlles;

import Entities.Feed;
import Entites.utilisateur;
import Services.FeedService;
import Services.EmailService;
import Services.FeedbackEmailService;
import Services.FeedbackAnalysisService;
import Services.utilisateurService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker.State;
import netscape.javascript.JSObject;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
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
    @FXML private WebView captchaWebView;
    @FXML private Label captchaErrorLabel;
    @FXML private Button mesReclamationsButton;
    @FXML private Button supervisionButton;
    @FXML private Button plusButton;
    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private VBox userMenuBox;
    
    private final FeedService feedService = new FeedService();
    private final FeedbackAnalysisService analysisService = new FeedbackAnalysisService();
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
    
    // Configuration reCAPTCHA
    private static final String RECAPTCHA_SITE_KEY = "6LflVyYrAAAAAKQoKc_WtUtuJRsqPDwe9oJcDvBo";
    private static final String RECAPTCHA_SECRET_KEY = "6LflVyYrAAAAAGN0dRyuSYieds5g7bwdoL5_YWB3";
    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private String captchaResponse = null;

    private HttpServer server;
    private static final int PORT = 8085;
    private int currentUserId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupValidation();
        setupCharacterCount();
        startLocalServer();
        setupCaptcha();
        
        // Initialize navigation elements
        boolean isLoggedIn = (currentUserId != 0);
        mesReclamationsButton.setVisible(isLoggedIn);
        supervisionButton.setVisible(isLoggedIn);
        plusButton.setVisible(isLoggedIn);
        userMenuBox.setVisible(false);
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
        validateCaptcha();
    }

    private boolean isFormValid() {
        return validateName() && validateEmail() && validateSubject() && validateComment() && validateCaptcha();
    }

    private Feed createFeed() {
        Feed feed = new Feed();
        feed.setName_feed(nameField.getText().trim());
        feed.setEmail_feed(emailField.getText().trim());
        feed.setSubject_feed(subjectField.getText().trim());
        feed.setCommentaire_feed(commentField.getText().trim());
        // Analyse du sentiment
        String sentiment = analysisService.analyzeFeedbackSentiment(feed.getCommentaire_feed()).name();
        feed.setSentiment(sentiment);
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

    private void startLocalServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/captcha", new CaptchaHandler());
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class CaptchaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
                    <style>
                        body { 
                            margin: 0; 
                            padding: 0;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            min-height: 200px;
                        }
                        #captcha-container {
                            width: 100%%;
                            height: 100%%;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                        }
                        .g-recaptcha {
                            transform-origin: center;
                            transform: scale(1.2);
                        }
                    </style>
                </head>
                <body>
                    <div id="captcha-container">
                        <div class="g-recaptcha" 
                             data-sitekey="%s"
                             data-callback="onCaptchaSuccess"
                             data-size="normal"></div>
                    </div>
                    <script>
                        var captchaResponse = null;
                        
                        function onCaptchaSuccess(token) {
                            console.log("Captcha validated with token:", token);
                            captchaResponse = token;
                            document.getElementById('captcha-container').dispatchEvent(
                                new CustomEvent('captchaValidated', { detail: token })
                            );
                        }

                        function checkCaptcha() {
                            return captchaResponse;
                        }
                    </script>
                </body>
                </html>
                """, RECAPTCHA_SITE_KEY);

            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html.getBytes());
            }
        }
    }

    public class JavaBridge {
        public void handleCaptchaResponse(String response) {
            if (response != null && !response.isEmpty()) {
                System.out.println("Captcha response received in JavaBridge");
                Platform.runLater(() -> {
                    captchaResponse = response;
                    captchaErrorLabel.setVisible(false);
                    System.out.println("Captcha response stored successfully");
                });
            }
        }
    }

    private void setupCaptcha() {
        captchaWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                try {
                    // Ajouter un écouteur d'événement pour le captcha
                    captchaWebView.getEngine().executeScript("""
                        document.getElementById('captcha-container').addEventListener('captchaValidated', function(e) {
                            window.java.handleCaptchaResponse(e.detail);
                        });
                    """);
                    
                    JSObject window = (JSObject) captchaWebView.getEngine().executeScript("window");
                    window.setMember("java", new JavaBridge());
                    System.out.println("JavaBridge installed successfully");
                } catch (Exception e) {
                    System.err.println("Error setting up JavaBridge: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        captchaWebView.getEngine().load("http://localhost:" + PORT + "/captcha");
    }

    private boolean validateCaptcha() {
        try {
            // Vérifier directement l'état du captcha via JavaScript
            JSObject window = (JSObject) captchaWebView.getEngine().executeScript("window");
            Object response = captchaWebView.getEngine().executeScript("checkCaptcha()");
            
            boolean isValid = response != null && !response.toString().equals("null");
            System.out.println("Validating captcha. Direct check response: " + response);
            
            if (!isValid) {
                showError(captchaErrorLabel, "Veuillez valider le captcha");
                return false;
            }
            captchaResponse = response.toString();
            return true;
        } catch (Exception e) {
            System.err.println("Error validating captcha: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleSubmit() {
        System.out.println("Handle submit called");
        
        // Vérifier d'abord le captcha
        if (!validateCaptcha()) {
            System.out.println("Captcha validation failed");
            return;
        }

        // Ensuite valider le reste du formulaire
        validateAll();

        if (isFormValid()) {
            System.out.println("Form is valid, proceeding with submission");
            try {
                String name = nameField.getText();
                String email = emailField.getText();
                String subject = subjectField.getText();
                String message = commentField.getText();
                
                // Create and send email
                System.out.println("Tentative d'envoi de l'email...");
                FeedbackEmailService emailService = new FeedbackEmailService();
                emailService.sendFeedbackEmail(email, name, subject, message);
                System.out.println("Email envoyé avec succès");
                
                // Save feedback to database
                Feed feed = createFeed();
                feedService.addFeed(feed);
                
                // Show success alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Votre feedback a été envoyé avec succès !");
                alert.showAndWait();
                
                // Clear form fields
                clearForm();
                
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi du feedback : " + e.getMessage());
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur est survenue lors de l'envoi : " + e.getMessage());
                alert.showAndWait();
            }
        } else {
            System.out.println("Form validation failed");
        }
    }

    @FXML
    void loginFront(ActionEvent event) {
        cleanup(); // Arrêter le serveur avant de changer de vue
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void InscFront(ActionEvent event) {
        cleanup(); // Arrêter le serveur avant de changer de vue
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterUtilisateur.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void accueilFront(ActionEvent event) {
        cleanup(); // Arrêter le serveur avant de changer de vue
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
            Parent root = loader.load();
            Controlles.FrontEnd controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanup() {
        if (server != null) {
            server.stop(0);
        }
    }

    // Appelez cleanup() quand la fenêtre se ferme
    private void setupWindowCloseHandler(Stage stage) {
        stage.setOnCloseRequest(event -> cleanup());
    }

    @FXML
    void submit(ActionEvent event) {
        try {
            String name = nameField.getText();
            String email = emailField.getText();
            String subject = subjectField.getText();
            String message = commentField.getText();
            
            // Create and send email
            EmailService emailService = new EmailService();
            emailService.sendFeedbackEmail(email, name, subject, message);
            
            // Save feedback to database
            Feed feed = new Feed();
            feed.setName_feed(name);
            feed.setEmail_feed(email);
            feed.setSubject_feed(subject);
            feed.setCommentaire_feed(message);
            feedService.addFeed(feed);
            
            // Show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Feedback submitted successfully!");
            alert.showAndWait();
            
            // Clear form fields
            nameField.clear();
            emailField.clear();
            subjectField.clear();
            commentField.clear();
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while submitting feedback: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void FrontProduit(ActionEvent event) {
        cleanup();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
            Parent root = loader.load();
            FrontEnd controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Produits");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMesReclamations(ActionEvent event) {
        cleanup();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ReclamationUser.fxml"));
            Parent root = loader.load();
            ReclamationUserController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupervision(ActionEvent event) {
        // TODO: Implémenter la redirection vers la page de supervision
        System.out.println("Redirection vers la page de supervision");
    }

    @FXML
    private void handlePlusButton(ActionEvent event) {
        boolean currentlyVisible = userMenuBox.isVisible();
        userMenuBox.setVisible(!currentlyVisible);
    }

    @FXML
    private void viewProfile(ActionEvent event) {
        cleanup();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
            Parent root = loader.load();
            ProfileUser controller = loader.getController();
            utilisateurService us = new utilisateurService();
            utilisateur currentUser = us.findByID(currentUserId);
            controller.setCurrentUserId(currentUser.getId_user());
            controller.setNomfx(currentUser.getNom_user());
            controller.setPrenomfx(currentUser.getPrenom_user());
            controller.setEmailfx(currentUser.getEmail_user());
            controller.setPwdfx(currentUser.getMot_de_passe_user());
            controller.setAdressfx(currentUser.getEmail_user());
            controller.setCodefx(currentUser.getCode_postal_user());
            controller.setTelfx(currentUser.getTelephone_user());
            controller.setVillefx(currentUser.getVille_user());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Profil utilisateur");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        cleanup();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        boolean isLoggedIn = (currentUserId != 0);
        mesReclamationsButton.setVisible(isLoggedIn);
        supervisionButton.setVisible(isLoggedIn);
        plusButton.setVisible(isLoggedIn);
    }
} 