package Controlles;

import javafx.fxml.FXML;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.scene.Scene;

public class DashboardController {
    @FXML
    private TreeView<String> sidebarMenu;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        setupSidebarMenu();
    }

    private void setupSidebarMenu() {
        sidebarMenu.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String selectedItem = newValue.getValue();
                System.out.println("Menu sélectionné : " + selectedItem);

                switch (selectedItem) {
                    case "User":
                        try {
                            System.out.println("Chargement de la vue User...");
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListUser.fxml"));
                            Parent view = loader.load();
                            contentArea.getChildren().clear();
                            contentArea.getChildren().add(view);
                            System.out.println("Vue User chargée avec succès");
                        } catch (IOException e) {
                            System.err.println("Erreur lors du chargement de la vue User:");
                            e.printStackTrace();
                        }
                        break;
                    case "Feedbacks":
                        System.out.println("Tentative de chargement des Feedbacks...");
                        loadView("/Views/BackFeed.fxml");
                        break;
                    case "Réclamations":
                        System.out.println("Tentative de chargement des Réclamations...");
                        loadView("/Views/BackReclamation.fxml");
                        break;
                    case "Commandes":
                        System.out.println("Tentative de chargement de la vue Livreur...");
                        loadView("/view/livreur-view.fxml");
                        break;
                    case "Produit":
                        System.out.println("Tentative de chargement de la vue Produit...");
                        loadView("/Produit.fxml");
                        break;
                    case "Categories":
                        System.out.println("Tentative de chargement de la vue Categories...");
                        loadView("/category_view.fxml");
                        break;
                    case "Rendu":
                        System.out.println("Tentative de chargement de la vue Rendu...");
                        loadView("/Views/RenduListView.fxml");
                        break;
                    case "Alert":
                        System.out.println("Tentative de chargement de la vue Alerte...");
                        loadView("/Views/AlerteListView.fxml");
                        break;
                }
            }
        });
    }
    
    private void loadView(String fxmlPath) {
        try {
            System.out.println("Tentative de chargement de : " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            System.out.println("Vue " + fxmlPath + " chargée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de : " + fxmlPath);
            System.err.println("Message d'erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            System.out.println("Déconnexion en cours...");
            // Charger la vue de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            
            // Récupérer la scène actuelle
            Scene scene = contentArea.getScene();
            
            // Remplacer le contenu de la scène
            scene.setRoot(root);
            
            System.out.println("Déconnexion réussie");
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion:");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProduit() {
        try {
            System.out.println("Chargement de la vue Produit...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Produit.fxml"));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            System.out.println("Vue Produit chargée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue Produit:");
            e.printStackTrace();
        }
    }
} 