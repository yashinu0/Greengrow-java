package Controlles;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import Entites.histaction;
import Entites.utilisateur;
import Services.actionService;
import Services.utilisateurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AjouterUtilisateur {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField adresse_user;

    @FXML
    private TextField code_postal_user;

    @FXML
    private PasswordField cpwd_user;

    @FXML
    private TextField email_user;

    @FXML
    private PasswordField mot_de_passe_user;


    @FXML
    private TextField nom_user;

    @FXML
    private TextField prenom_user;

    @FXML
    private TextField telephone_user;

    @FXML
    private TextField ville_user;

    @FXML
    private void ajouterU(ActionEvent event) {
        String erreurs = "";

        if (nom_user.getText().isEmpty()) {
            erreurs += "- Le nom est requis.\n";
        }

        if (prenom_user.getText().isEmpty()) {
            erreurs += "- Le prénom est requis.\n";
        }

        if (email_user.getText().isEmpty()) {
            erreurs += "- L'email est requis.\n";
        } else {
            String email = email_user.getText();
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z]+(\\.[A-Za-z]{2,3})$")) {
                erreurs += "- L'email doit être sous la forme exemple@domaine.com.\n";
            }
        }

        if (mot_de_passe_user.getText().isEmpty()) {
            erreurs += "- Le mot de passe est requis.\n";
        }

        if (cpwd_user.getText().isEmpty()) {
            erreurs += "- La confirmation du mot de passe est requise.\n";
        }

        if (!mot_de_passe_user.getText().equals(cpwd_user.getText())) {
            erreurs += "- Les mots de passe ne correspondent pas.\n";
        }

        if (adresse_user.getText().isEmpty()) {
            erreurs += "- L'adresse est requise.\n";
        }

        if (code_postal_user.getText().isEmpty()) {
            erreurs += "- Le code postal est requis.\n";
        } else if (!code_postal_user.getText().matches("\\d{4}")) {
            erreurs += "- Le code postal doit contenir 4 chiffres.\n";
        }

        if (telephone_user.getText().isEmpty()) {
            erreurs += "- Le numéro de téléphone est requis.\n";
        } else if (!telephone_user.getText().matches("\\d{8}")) {
            erreurs += "- Le téléphone doit contenir 8 chiffres.\n";
        }

        if (ville_user.getText().isEmpty()) {
            erreurs += "- La ville est requise.\n";
        }

        if (!erreurs.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs invalides");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(erreurs);
            alert.show();
            return;
        }


            utilisateur u = new utilisateur(
                nom_user.getText(),
                prenom_user.getText(),
                email_user.getText(),
                mot_de_passe_user.getText(),
                "ROLE_CLIENT",
                adresse_user.getText(),
                code_postal_user.getText(),
                telephone_user.getText(),
                ville_user.getText(),
                true
        );

        utilisateurService utilisateurService = new utilisateurService();
        int id = utilisateurService.addAndReturnId(u);
        u.setId_user(id);
        actionService actionService = new actionService();
        utilisateur userAction = new utilisateur();
        userAction.setId_user(id);
        histaction action = new histaction(
                userAction,
                "AJOUT",
                "Nouvel utilisateur créé : " + u.getEmail_user(),
                LocalDateTime.now()
        );
        actionService.addAction(action);
        System.out.println(action);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("L'utilisateur a été ajouté avec succès");
        alert.show();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
            Parent root = loader.load();
            ProfileUser profileUser = loader.getController();

            profileUser.setCurrentUserId(id);
            profileUser.setNomfx(nom_user.getText());
            profileUser.setPrenomfx(prenom_user.getText());
            profileUser.setEmailfx(email_user.getText());
            profileUser.setPwdfx(mot_de_passe_user.getText());
            profileUser.setTelfx(telephone_user.getText());
            profileUser.setAdressfx(adresse_user.getText());
            profileUser.setCodefx(code_postal_user.getText());
            profileUser.setVillefx(ville_user.getText());

            nom_user.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert1 = new Alert(Alert.AlertType.ERROR);
            alert1.setContentText("Erreur lors du chargement du profil !");
            alert1.show();
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        nom_user.clear();
        prenom_user.clear();
        email_user.clear();
        mot_de_passe_user.clear();
        cpwd_user.clear();
        adresse_user.clear();
        code_postal_user.clear();
        telephone_user.clear();
        ville_user.clear();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Le formulaire a été réinitialisé.");
        alert.show();
    }

    @FXML
    void initialize() {
    }
}
