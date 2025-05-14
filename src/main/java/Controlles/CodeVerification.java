package Controlles;

import Utils.sendEmail;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Services.utilisateurService;

import java.io.IOException;
import java.util.Random;

public class CodeVerification {
    @FXML private TextField codeField;
    @FXML private Label emailLabel;
    @FXML private Label errorLabel;

    private String userEmail;
    private String verificationCode;
    private utilisateurService userService;

    public CodeVerification() {
        this.userService = new utilisateurService();
    }

    public void setUserEmailAndCode(String email, String code) {
        this.userEmail = email;
        this.verificationCode = code;
        emailLabel.setText("Code envoyé à : " + email);
    }

    @FXML
    void verifyCodeFx(ActionEvent event) {
        String enteredCode = codeField.getText().trim();
        if (enteredCode.isEmpty()) {
            showError("Champ vide", "Veuillez entrer le code de vérification.");
            return;
        }
        
        if (enteredCode.equals(verificationCode)) {
            loadPasswordResetScreen();
        } else {
            showError("Code incorrect", "Le code de vérification est incorrect. Essayez à nouveau.");
        }
    }

    @FXML
    void handleBackToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) codeField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page de connexion.");
        }
    }

    @FXML
    void handleResendCode(ActionEvent event) {
        // Générer un nouveau code
        String newCode = generateVerificationCode();
        sendEmail send = new sendEmail();


        // Envoyer le nouveau code par email
        if (send.sendVerificationEmail(userEmail, newCode)) {
            showSuccess("Code renvoyé", "Un nouveau code de vérification a été envoyé à votre adresse email.");
            codeField.clear();
        } else {
            showError("Erreur", "Impossible d'envoyer le nouveau code. Veuillez réessayer.");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        handleBackToLogin(event);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Génère un code à 6 chiffres
        return String.valueOf(code);
    }

    private void showError(String title, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadPasswordResetScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPasswordController.fxml"));
            Parent root = loader.load();

            ResetPasswordController controller = loader.getController();
            controller.setUserEmail(userEmail);

            Stage stage = (Stage) codeField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page de réinitialisation du mot de passe.");
        }
    }
}
