package Controlles;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import Entites.histaction;
import Entites.utilisateur;
import Services.actionService;
import Services.utilisateurService;
import Utils.EmailVerifier;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;

public class AjouterUtilisateur {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField adresse_user;
    @FXML private TextField code_postal_user;
    @FXML private PasswordField cpwd_user;
    @FXML private TextField email_user;
    @FXML private PasswordField mot_de_passe_user;
    @FXML private TextField nom_user;
    @FXML private TextField prenom_user;
    @FXML private TextField telephone_user;
    @FXML private TextField ville_user;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z]+(\\.[A-Za-z]{2,3})$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\d{8}");
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("\\d{4}");

    @FXML
    void initialize() {
        setupValidation();
        setupTooltips();
    }




    private void setupValidation() {
        // Validation en temps réel pour l'email
        email_user.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!EMAIL_PATTERN.matcher(newVal).matches()) {
                email_user.setStyle("-fx-border-color: red;");
            } else {
                email_user.setStyle("");
            }
        });

        // Validation en temps réel pour le téléphone
        telephone_user.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!PHONE_PATTERN.matcher(newVal).matches()) {
                telephone_user.setStyle("-fx-border-color: red;");
            } else {
                telephone_user.setStyle("");
            }
        });

        // Validation en temps réel pour le code postal
        code_postal_user.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!POSTAL_CODE_PATTERN.matcher(newVal).matches()) {
                code_postal_user.setStyle("-fx-border-color: red;");
            } else {
                code_postal_user.setStyle("");
            }
        });

        // Validation en temps réel pour le mot de passe
        mot_de_passe_user.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 6) {
                mot_de_passe_user.setStyle("-fx-border-color: red;");
            } else {
                mot_de_passe_user.setStyle("");
            }
        });

        // Validation en temps réel pour la confirmation du mot de passe
        cpwd_user.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(mot_de_passe_user.getText())) {
                cpwd_user.setStyle("-fx-border-color: red;");
            } else {
                cpwd_user.setStyle("");
            }
        });
    }

    private void setupTooltips() {
        email_user.setTooltip(new Tooltip("Format: exemple@domaine.com"));
        telephone_user.setTooltip(new Tooltip("8 chiffres requis"));
        code_postal_user.setTooltip(new Tooltip("4 chiffres requis"));
        mot_de_passe_user.setTooltip(new Tooltip("Minimum 6 caractères"));
    }

    @FXML
    private void ajouterU(ActionEvent event) {
        if (!validateAllFields()) {
            return;
        }

        try {
            utilisateur u = new utilisateur(
                nom_user.getText().trim(),
                prenom_user.getText().trim(),
                email_user.getText().trim(),
                mot_de_passe_user.getText(),
                "ROLE_CLIENT",
                adresse_user.getText().trim(),
                code_postal_user.getText().trim(),
                telephone_user.getText().trim(),
                ville_user.getText().trim(),
                true
            );

            utilisateurService utilisateurService = new utilisateurService();
            
            // Vérifier si l'email existe déjà
            if (utilisateurService.findByEmail(u.getEmail_user()) != null) {
                showError("Cet email est déjà utilisé");
                return;
            }

            int id = utilisateurService.addAndReturnId(u);
            u.setId_user(id);

            // Enregistrer l'action
            actionService actionService = new actionService();
            histaction action = new histaction(
                u,
                "AJOUT",
                "Nouvel utilisateur créé : " + u.getEmail_user(),
                LocalDateTime.now()
            );
            actionService.addAction(action);

            showSuccess("Inscription réussie !");
            loadProfilePage(id);

        } catch (Exception e) {
            showError("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    private boolean validateAllFields() {
        StringBuilder errors = new StringBuilder();

        if (nom_user.getText().trim().isEmpty()) {
            errors.append("- Le nom est requis\n");
        }

        if (prenom_user.getText().trim().isEmpty()) {
            errors.append("- Le prénom est requis\n");
        }

        if (!EMAIL_PATTERN.matcher(email_user.getText().trim()).matches()) {
            errors.append("- Format d'email invalide\n");
        }
        String email = email_user.getText();
        if (!EmailVerifier.verifyEmail(email)) {
            System.out.println(EmailVerifier.verifyEmail(email));
            errors.append("Adresse email invalide ou non vérifiable ");

        }


        if (mot_de_passe_user.getText().length() < 6) {
            errors.append("- Le mot de passe doit contenir au moins 6 caractères\n");
        }

        if (!mot_de_passe_user.getText().equals(cpwd_user.getText())) {
            errors.append("- Les mots de passe ne correspondent pas\n");
        }

        if (!PHONE_PATTERN.matcher(telephone_user.getText().trim()).matches()) {
            errors.append("- Le numéro de téléphone doit contenir 8 chiffres\n");
        }

        if (adresse_user.getText().trim().isEmpty()) {
            errors.append("- L'adresse est requise\n");
        }

        if (!POSTAL_CODE_PATTERN.matcher(code_postal_user.getText().trim()).matches()) {
            errors.append("- Le code postal doit contenir 4 chiffres\n");
        }

        if (ville_user.getText().trim().isEmpty()) {
            errors.append("- La ville est requise\n");
        }

        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadProfilePage(int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
            Parent root = loader.load();
            ProfileUser profileUser = loader.getController();

            profileUser.setCurrentUserId(userId);
            profileUser.setNomfx(nom_user.getText().trim());
            profileUser.setPrenomfx(prenom_user.getText().trim());
            profileUser.setEmailfx(email_user.getText().trim());
            profileUser.setPwdfx(mot_de_passe_user.getText());
            profileUser.setTelfx(telephone_user.getText().trim());
            profileUser.setAdressfx(adresse_user.getText().trim());
            profileUser.setCodefx(code_postal_user.getText().trim());
            profileUser.setVillefx(ville_user.getText().trim());

            Stage stage = (Stage) nom_user.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Profil Utilisateur");
        } catch (IOException e) {
            showError("Erreur lors du chargement du profil : " + e.getMessage());
        }
    }

    @FXML
    void annuler(ActionEvent event) {
        clearFields();
        showSuccess("Formulaire réinitialisé");
    }

    private void clearFields() {
        nom_user.clear();
        prenom_user.clear();
        email_user.clear();
        mot_de_passe_user.clear();
        cpwd_user.clear();
        adresse_user.clear();
        code_postal_user.clear();
        telephone_user.clear();
        ville_user.clear();
        
        // Réinitialiser les styles
        nom_user.setStyle("");
        prenom_user.setStyle("");
        email_user.setStyle("");
        mot_de_passe_user.setStyle("");
        cpwd_user.setStyle("");
        adresse_user.setStyle("");
        code_postal_user.setStyle("");
        telephone_user.setStyle("");
        ville_user.setStyle("");
    }

    @FXML
    void FrontEndFx(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FrontEnd.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page d'accueil : " + e.getMessage());
        }
    }

    @FXML
    void loginFront(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de connexion : " + e.getMessage());
        }
    }
}
