package Controlles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Utils.sendEmail;
import Entites.utilisateur;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TwoFactorAuth implements Initializable {
    @FXML
    private TextField codeField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Button verifyButton;
    @FXML
    private Button resendButton;

    private String userEmail;
    private String verificationCode;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setVisible(false);
    }

    public void setUserEmailAndCode(String email, String code) {
        this.userEmail = email;
        this.verificationCode = code;
        emailLabel.setText("Code de sécurité envoyé à : " + email);
    }

    @FXML
    private void verifyCode() {
        String enteredCode = codeField.getText().trim();
        
        if (enteredCode.isEmpty()) {
            showError("Veuillez entrer le code de sécurité");
            return;
        }

        if (enteredCode.equals(verificationCode)) {
            utilisateur tempUser = SessionManager.getInstance().getTempUser();
            if (tempUser != null) {
                // Stocker l'utilisateur dans la session
                SessionManager.getInstance().setCurrentUser(tempUser);
                
                try {
                    // Rediriger selon le rôle
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
                showError("Erreur de session");
            }
        } else {
            showError("Code de sécurité incorrect");
        }
    }

    private void loadClientInterface(utilisateur user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
        Parent root = loader.load();
        FrontEnd controller = loader.getController();
        
        controller.setCurrentUserId(user.getId_user());
        controller.generateAvatar(user.getNom_user());
        
        Stage stage = (Stage) verifyButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Mon profil");
    }

    private void loadAdminInterface() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/DashboardTemplate.fxml"));
        Parent root = loader.load();
        
        Stage stage = (Stage) verifyButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Dashboard Administrateur");
    }

    @FXML
    private void resendCode() {
        String newCode = generateVerificationCode();
        sendEmail send = new sendEmail();
        
        if (send.sendVerificationEmail(userEmail, newCode)) {
            this.verificationCode = newCode;
            SessionManager.getInstance().setVerificationCode(newCode);
            showError("Nouveau code envoyé avec succès");
        } else {
            showError("Erreur lors de l'envoi du nouveau code");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private String generateVerificationCode() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }
} 