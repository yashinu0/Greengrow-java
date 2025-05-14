package Controlles;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import Entites.utilisateur;
import Services.utilisateurService;
import com.google.api.client.json.JsonFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import Utils.sendEmail;
import javafx.application.Platform;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.UUID;

public class login implements Initializable {
    @FXML
    private TextField emailfx;
    @FXML
    private PasswordField mdpFx;
    @FXML
    private Button loginButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button forgotPasswordButton;
    @FXML
    private Button createAccountButton;
    @FXML
    private Button googleLoginButton;
    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupValidation();
        setupErrorHandling();
    }

    private void setupValidation() {
        // Validation de l'email
        emailfx.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                emailfx.setStyle("-fx-border-color: red;");
            } else {
                emailfx.setStyle("");
            }
        });

        // Validation du mot de passe
        mdpFx.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 6) {
                mdpFx.setStyle("-fx-border-color: red;");
            } else {
                mdpFx.setStyle("");
            }
        });
    }

    private void setupErrorHandling() {
        errorLabel.setVisible(false);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    @FXML
    void loginFx(ActionEvent event) {
        String email = emailfx.getText().trim();
        String password = mdpFx.getText().trim();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Format d'email invalide");
            return;
        }

        utilisateurService us = new utilisateurService();
        utilisateur u = us.login(email, password);

        if (u != null) {
            // Générer et envoyer le code de vérification
            String verificationCode = generateVerificationCode();
            sendEmail send = new sendEmail();
            
            if (send.sendVerificationEmail(email, verificationCode)) {
                // Stocker temporairement l'utilisateur et le code
                SessionManager.getInstance().setTempUser(u);
                SessionManager.getInstance().setVerificationCode(verificationCode);
                
                // Charger l'écran de double authentification
                loadTwoFactorAuthScreen(email, verificationCode);
            } else {
                showError("Erreur lors de l'envoi du code de vérification");
            }
        } else {
            showError("Email ou mot de passe incorrect");
        }
    }

    private void loadTwoFactorAuthScreen(String email, String code) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TwoFactorAuth.fxml"));
            Parent root = loader.load();

            TwoFactorAuth controller = loader.getController();
            controller.setUserEmailAndCode(email, code);

            Stage stage = (Stage) emailfx.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur de chargement de l'écran de vérification");
            e.printStackTrace();
        }
    }

    public void verifyAndLogin(String code) {
        String storedCode = SessionManager.getInstance().getVerificationCode();
        utilisateur tempUser = SessionManager.getInstance().getTempUser();

        if (storedCode != null && storedCode.equals(code)) {
            // Vérifier si la session précédente est expirée
            if (SessionManager.getInstance().isSessionExpired()) {
                SessionManager.getInstance().clearSession();
            }

            // Stocker l'utilisateur dans la session
            SessionManager.getInstance().setCurrentUser(tempUser);

            try {
                if (tempUser.getRole_user().equals("ROLE_CLIENT")) {
                    loadClientInterface(tempUser);
                } else {
                    loadAdminInterface();
                }
            } catch (IOException e) {
                showError("Erreur de chargement de l'interface");
                e.printStackTrace();
            }
        } else {
            showError("Code de vérification incorrect");
        }
    }

    private void loadClientInterface(utilisateur user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
        Parent root = loader.load();
        FrontEnd controller = loader.getController();
        
        // Configuration du contrôleur avec les informations de l'utilisateur
        controller.setCurrentUserId(user.getId_user());
        controller.generateAvatar(user.getNom_user());
        
        // Changement de scène
        Stage stage = (Stage) emailfx.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Mon profil");
    }

    private void loadAdminInterface() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/DashboardTemplate.fxml"));
        Parent root = loader.load();
        DashboardController controller = loader.getController();
        
        Stage stage = (Stage) emailfx.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Dashboard Administrateur");
    }

    @FXML
    void annulerfx(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        emailfx.clear();
        mdpFx.clear();
        errorLabel.setVisible(false);
        emailfx.setStyle("");
        mdpFx.setStyle("");
    }

    @FXML
    void oubliefx(ActionEvent event) {
        String email = emailfx.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Format d'email invalide");
            return;
        }

        utilisateurService us = new utilisateurService();
        utilisateur user = us.findByEmail(email);

        if (user == null) {
            showError("Aucun compte associé à cet email");
            return;
        }

        String verificationCode = generateVerificationCode();
        sendEmail send = new sendEmail();
        
        if (send.sendVerificationEmail(email, verificationCode)) {
            loadCodeVerificationScreen(email, verificationCode);
        } else {
            showError("Erreur lors de l'envoi du code de vérification");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void loadCodeVerificationScreen(String email, String code) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CodeVerification.fxml"));
            Parent root = loader.load();

            CodeVerification controller = loader.getController();
            controller.setUserEmailAndCode(email, code);

            Stage stage = (Stage) emailfx.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur de chargement de l'écran de vérification");
            e.printStackTrace();
        }
    }

    @FXML
    void ajouterFx(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterUtilisateur.fxml"));
            Stage stage = (Stage) emailfx.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un compte");
        } catch (IOException e) {
            showError("Erreur de chargement de la page d'inscription");
            e.printStackTrace();
        }
    }

    @FXML
    void FrontEndFx(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FrontEnd.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (IOException e) {
            showError("Erreur de chargement de la page d'accueil");
            e.printStackTrace();
        }
    }

    @FXML
    void InscFrontFx(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterUtilisateur.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
        } catch (IOException e) {
            showError("Erreur de chargement de la page d'inscription");
            e.printStackTrace();
        }
    }

    @FXML
    void loginWithGoogle(ActionEvent event) {
        try {
            // Configuration du flux OAuth2
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                    new InputStreamReader(getClass().getResourceAsStream("/client_secret.json")));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    jsonFactory,
                    clientSecrets,
                    Arrays.asList(
                            "https://www.googleapis.com/auth/userinfo.profile",
                            "https://www.googleapis.com/auth/userinfo.email"))
                    .setDataStoreFactory(new MemoryDataStoreFactory())
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8890).build();
            
            new Thread(() -> {
                try {
                    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

                    // Récupération des informations utilisateur
                    Oauth2 oauth2 = new Oauth2.Builder(
                            GoogleNetHttpTransport.newTrustedTransport(),
                            jsonFactory,
                            credential)
                            .setApplicationName("Green Grow")
                            .build();

                    Userinfo userInfo = oauth2.userinfo().get().execute();

                    // Traitement dans le thread UI
                    Platform.runLater(() -> {
                        handleGoogleUser(userInfo);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showError("Erreur d'authentification Google : " + e.getMessage());
                    });
                }
            }).start();

        } catch (Exception e) {
            showError("Erreur d'initialisation Google : " + e.getMessage());
        }
    }

    private void handleGoogleUser(Userinfo userInfo) {
        try {
            utilisateurService us = new utilisateurService();
            utilisateur user = us.findByEmail(userInfo.getEmail());

            // Création du compte si inexistant
            if (user == null) {
                user = new utilisateur();
                user.setNom_user(userInfo.getFamilyName());
                user.setPrenom_user(userInfo.getGivenName());
                user.setEmail_user(userInfo.getEmail());
                user.setRole_user("ROLE_CLIENT");
                user.setMot_de_passe_user(generateRandomPassword());
                us.addAndReturnId(user);
            }

            // Stocker l'utilisateur dans la session
            SessionManager.getInstance().setCurrentUser(user);

            // Redirection selon le rôle
            if (user.getRole_user().equals("ROLE_CLIENT")) {
                loadClientInterface(user);
            } else {
                loadAdminInterface();
            }
        } catch (Exception e) {
            showError("Erreur de traitement : " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }
}


