package Controlles;

import Entites.histaction;
import Services.actionService;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

import Entites.utilisateur;
import Services.utilisateurService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProfileUser {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField idfx;
    @FXML
    private TextField adressfx;

    @FXML
    private TextField codefx;

    @FXML
    private TextField emailfx;

    @FXML
    private TextField nomfx;

    @FXML
    private TextField prenomfx;

    @FXML
    private PasswordField pwdfx;

    @FXML
    private TextField telfx;

    @FXML
    private TextField villefx;

    private int currentUserId;  // ID de l'utilisateur en cours

    // Setters pour injecter les données
    public void setAdressfx(String adressfx) {
        this.adressfx.setText(adressfx);
    }

    public void setCodefx(String codefx) {
        this.codefx.setText(codefx);
    }

    public void setEmailfx(String emailfx) {
        this.emailfx.setText(emailfx);
    }

    public void setPrenomfx(String prenomfx) {
        this.prenomfx.setText(prenomfx);
    }

    public void setNomfx(String nomfx) {
        this.nomfx.setText(nomfx);
    }

    public void setPwdfx(String pwdfx) {
        this.pwdfx.setText(pwdfx);
    }

    public void setTelfx(String telfx) {
        this.telfx.setText(telfx);
    }

    public void setVillefx(String villefx) {
        this.villefx.setText(villefx);
    }

    // Setter pour l'ID de l'utilisateur
    public void setCurrentUserId(int id) {
        this.currentUserId = id;
        System.out.println("Current User ID: " + currentUserId);  // Vérifiez l'ID ici
    }

    @FXML
    void initialize() {
    }

    @FXML
    void Desactivefx(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Voulez-vous vraiment désactiver votre compte ?");
        alert.setContentText("Cette action désactivera votre accès à l'application.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            utilisateurService us = new utilisateurService();
            us.desactiver(currentUserId);
            actionService actionService = new actionService();
            utilisateur userAction = new utilisateur();
            userAction.setId_user(currentUserId);
            histaction action = new histaction(
                    userAction,
                    "DESACTIVATION",
                    "Compte désactivé : " + emailfx.getText(),
                    LocalDateTime.now()
            );
            actionService.addAction(action);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Compte désactivé");
            success.setHeaderText(null);
            success.setContentText("Votre compte a été désactivé avec succès.");
            success.showAndWait();
            Platform.exit();
        }
    }


    @FXML
    private void updateFx(ActionEvent event) {
        String erreurs = "";

        if (nomfx.getText().isEmpty()) {
            erreurs += "- Le nom est requis.\n";
        }

        if (prenomfx.getText().isEmpty()) {
            erreurs += "- Le prénom est requis.\n";
        }
        if (emailfx.getText().isEmpty()) {
            erreurs += "- L'email est requis.\n";
        } else {
            String email = emailfx.getText();
            // Vérification du format de l'email avec une regex plus explicite
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z]+(\\.[A-Za-z]{2,3})$")) {
                erreurs += "- L'email doit être sous la forme exemple@domaine.com.\n";
            }
        }

        if (pwdfx.getText().isEmpty()) {
            erreurs += "- Le mot de passe est requis.\n";
        }

        if (adressfx.getText().isEmpty()) {
            erreurs += "- L'adresse est requise.\n";
        }

        if (codefx.getText().isEmpty()) {
            erreurs += "- Le code postal est requis.\n";
        } else if (!codefx.getText().matches("\\d{4}")) {
            erreurs += "- Le code postal doit contenir exactement 4 chiffres.\n";
        }

        if (telfx.getText().isEmpty()) {
            erreurs += "- Le numéro de téléphone est requis.\n";
        } else if (!telfx.getText().matches("\\d{8}")) {
            erreurs += "- Le numéro de téléphone doit contenir exactement 8 chiffres.\n";
        }

        if (villefx.getText().isEmpty()) {
            erreurs += "- La ville est requise.\n";
        }

        // Si erreurs, on les affiche
        if (!erreurs.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs invalides");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(erreurs);
            alert.show();
            return;
        }
        utilisateur updatedUser = new utilisateur(
                nomfx.getText(),
                prenomfx.getText(),
                emailfx.getText(),
                pwdfx.getText(),
                "ROLE_CLIENT",
                adressfx.getText(),
                codefx.getText(),
                telfx.getText(),
                villefx.getText(),
                true
        );
        updatedUser.setId_user(currentUserId); // TRÈS IMPORTANT

        utilisateurService us = new utilisateurService();
        us.update(updatedUser);
        actionService actionService = new actionService();
        utilisateur userAction = new utilisateur();
        userAction.setId_user(currentUserId);
        histaction action = new histaction(
                userAction,
                "MODIFICATION",
                "Mise à jour du profil utilisateur: "+  emailfx.getText(),
                LocalDateTime.now()
        );
        actionService.addAction(action);
        System.out.println(currentUserId);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mise à jour");
        alert.setHeaderText(null);
        alert.setContentText("Profil mis à jour !");
        alert.showAndWait();
    }
    @FXML
    void homefx(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));

        try {
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Accueil");
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) nomfx.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    }



