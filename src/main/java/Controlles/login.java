package Controlles;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import Entites.utilisateur;
import Services.utilisateurService;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import Utils.sendEmail;

public class login {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField emailfx;

    @FXML
    private PasswordField mdpFx;

    @FXML
    void initialize() {
    }

    @FXML
    void annulerfx(ActionEvent event) {
    }

    @FXML
    void loginFx(ActionEvent event) {
        String email = emailfx.getText();
        String password = mdpFx.getText();

        utilisateurService us = new utilisateurService();
        utilisateur u = us.login(email, password);

        if (u != null) {
            if (u.getRole_user().equals("ROLE_CLIENT")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
                try {
                    Parent root = loader.load();
                    ProfileUser controller = loader.getController();
                    controller.setCurrentUserId(u.getId_user());
                    controller.setNomfx(u.getNom_user());
                    controller.setPrenomfx(u.getPrenom_user());
                    controller.setEmailfx(u.getEmail_user());
                    controller.setPwdfx(u.getMot_de_passe_user());
                    controller.setAdressfx(u.getAdresse_user());
                    controller.setCodefx(u.getCode_postal_user());
                    controller.setTelfx(u.getTelephone_user());
                    controller.setVillefx(u.getVille_user());

                    Stage stage = (Stage) emailfx.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Mon profil");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
                try {
                    Parent root = loader.load();
                    Stage stage = (Stage) emailfx.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Liste des utilisateurs");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de connexion");
            alert.setHeaderText("Identifiants incorrects");
            alert.setContentText("Vérifiez votre email et mot de passe.");
            alert.show();
        }
    }

    @FXML
    void ajouterFx(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AjouterUtilisateur.fxml"));
            Stage stage = (Stage) emailfx.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void oubliefx(ActionEvent event) {
        String email = emailfx.getText().trim();

        if (email.isEmpty()) {
            showAlert("Erreur", "Veuillez entrer votre email");
            return;
        }

        utilisateurService us = new utilisateurService();
        utilisateur user = us.findByEmail(email);

        if (user == null) {
            showAlert("Erreur", "Aucun compte associé à cet email");
            return;
        }

        String verificationCode = generateVerificationCode();
        sendEmail send = new sendEmail();
        if (send.sendVerificationEmail(email, verificationCode)) {
            System.out.println("Code envoyé à l'utilisateur : " + verificationCode);
        }

        loadCodeVerificationScreen(email, verificationCode);
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
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void oubliefx1(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ForgotPassword.fxml"));
            Stage stage = (Stage) emailfx.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
