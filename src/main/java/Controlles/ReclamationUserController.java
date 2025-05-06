package Controlles;

import Entities.Reclamation;
import Entites.utilisateur;
import Services.ReclamationService;
import Services.utilisateurService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.io.IOException;

public class ReclamationUserController implements Initializable {
    @FXML private FlowPane reclamationsContainer;
    @FXML private Button backButton;
    @FXML private Button loginButton;
    @FXML public Label nomLabel;
    @FXML public Label prenomLabel;
    @FXML private VBox userMenuBox;
    @FXML private Button plusButton;
    
    private final ReclamationService reclamationService = new ReclamationService();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private int currentUserId;

    @FXML
    public void accueilFront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
            Parent root = loader.load();
            // Passer l'ID utilisateur au contrôleur FrontEnd
            FrontEnd controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void InscFront(ActionEvent event) {
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
    public void FrontProduit(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/produit.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Produits");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loginFront(ActionEvent event) {
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
    private void handleMesReclamations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ReclamationUser.fxml"));
            Parent root = loader.load();
            
            // Passe l'ID utilisateur au nouveau contrôleur
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
        // Inverse la visibilité du menu utilisateur
        VBox userMenuBox = (VBox) ((Node) event.getSource()).getScene().lookup("#userMenuBox");
        if (userMenuBox != null) {
            boolean currentlyVisible = userMenuBox.isVisible();
            userMenuBox.setVisible(!currentlyVisible);
        }
    }

    @FXML
    public void viewProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur
            ProfileUser controller = loader.getController();

            // Récupérer les données utilisateur
            utilisateurService us = new utilisateurService();
            utilisateur currentUser = us.findByID(currentUserId);

            // Injecter les données dans le contrôleur
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
        loadReclamations();

        // Récupérer l'utilisateur et afficher son nom/prénom
        utilisateurService us = new utilisateurService();
        utilisateur currentUser = us.findByID(userId);
        if (currentUser != null) {
            nomLabel.setText(currentUser.getNom_user());
            prenomLabel.setText(currentUser.getPrenom_user());
        } else {
            nomLabel.setText("");
            prenomLabel.setText("");
        }
        boolean isLoggedIn = (currentUserId != 0);
        if (plusButton != null) plusButton.setVisible(isLoggedIn);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBackButton();
        userMenuBox.setVisible(false);
        if (currentUserId != 0) {
            utilisateurService us = new utilisateurService();
            utilisateur currentUser = us.findByID(currentUserId);
            if (currentUser != null) {
                nomLabel.setText(currentUser.getNom_user());
                prenomLabel.setText(currentUser.getPrenom_user());
            }
            if (plusButton != null) plusButton.setVisible(true);
        } else {
            if (plusButton != null) plusButton.setVisible(false);
        }
    }

    private void setupBackButton() {
        backButton.setOnAction(event -> handleBack());
    }

    private void loadReclamations() {
        if (currentUserId == 0) {
            System.out.println("Aucun utilisateur connecté");
            return;
        }
        
        List<Reclamation> reclamations = reclamationService.getReclamationsByUser(currentUserId);
        
        reclamationsContainer.getChildren().clear();
        
        for (Reclamation reclamation : reclamations) {
            VBox card = createReclamationCard(reclamation);
            reclamationsContainer.getChildren().add(card);
        }
    }

    private VBox createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-pref-width: 400;");
        
        HBox header = new HBox(10);
        header.setStyle("-fx-alignment: CENTER_LEFT;");
        
        Label typeLabel = new Label(reclamation.getDescription_rec());
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label dateLabel = new Label(dateFormatter.format(reclamation.getDate_rec()));
        dateLabel.setStyle("-fx-text-fill: #6c757d;");
        
        Label statusLabel = new Label(reclamation.getStatut_rec());
        String baseStyle = "-fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-size: 12px;";
        switch (reclamation.getStatut_rec()) {
            case "Pending":
                statusLabel.setStyle(baseStyle + "-fx-text-fill: white; -fx-background-color: rgb(255, 46, 46);");
                break;
            case "In Progress":
                statusLabel.setStyle(baseStyle + "-fx-text-fill: white; -fx-background-color: rgb(255, 193, 7);");
                break;
            case "Resolved":
                statusLabel.setStyle(baseStyle + "-fx-text-fill: white; -fx-background-color: #28a745;");
                break;
            default:
                statusLabel.setStyle(baseStyle + "-fx-text-fill: white; -fx-background-color: #6c757d;");
        }
        
        header.getChildren().addAll(typeLabel, dateLabel, statusLabel);
        
        TextArea messageArea = new TextArea(reclamation.getMessage_reclamation());
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6;");
        
        Button viewButton = new Button("Voir les détails");
        viewButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        viewButton.setOnAction(event -> handleViewDetails(reclamation));
        
        card.getChildren().addAll(header, messageArea, viewButton);
        return card;
    }

    private void handleViewDetails(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ReclamationUserDetails.fxml"));
            Parent root = loader.load();
            
            ReclamationUserDetailsController controller = loader.getController();
            controller.setReclamation(reclamation);
            
            // Récupérer la scène actuelle et la remplacer
            Stage stage = (Stage) reclamationsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de la réclamation");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
            Parent root = loader.load();
            
            // Passer l'ID de l'utilisateur au contrôleur FrontEnd
            FrontEnd controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 