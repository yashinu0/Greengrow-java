package Controlles;

import java.io.IOException;
import java.net.URL;
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListUser.fxml"));
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
    void ajouterFx(ActionEvent event){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUtilisateur.fxml"));

        try {
            Parent root = loader.load();
            Stage stage = (Stage) emailfx.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
