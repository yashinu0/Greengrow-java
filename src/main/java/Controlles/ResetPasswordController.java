package Controlles;

import Services.utilisateurService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class ResetPasswordController{
    @FXML
    private PasswordField newPasswordField;
    private String userEmail;

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    void resetPasswordFx(ActionEvent event) {
        String newPassword = newPasswordField.getText();

        if(newPassword.isEmpty()) {
            System.out.println("Erreur Veuillez entrer un nouveau mot de passe");
            return;
        }

        utilisateurService us = new utilisateurService();
        if(us.updatePassword(userEmail, newPassword)) {
            System.out.println("Succès Mot de passe mis à jour avec succès");
            // Retour à l'écran de login
            loadLoginScreen();
        } else {
            System.out.println("Erreur Échec de la mise à jour du mot de passe");
        }
    }

    private void loadLoginScreen() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) newPasswordField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}