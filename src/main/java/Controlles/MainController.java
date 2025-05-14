package Controlles;

import view.LivreurView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MainController {
    @FXML
    private void openCommandesView() {
        try {
            Stage stage = new Stage();
            
            // Obtenir l'URL du fichier FXML de plusieurs façons
            URL fxmlUrl = null;
            
            // Méthode 1: Utiliser le ClassLoader
            fxmlUrl = MainController.class.getClassLoader()
                    .getResource("/view/commande-view.fxml");
            
            if (fxmlUrl == null) {
                // Méthode 2: Utiliser le chemin absolu
                fxmlUrl = MainController.class
                    .getResource("/view/commande-view.fxml");
            }
            
            if (fxmlUrl == null) {
                throw new IOException("Le fichier FXML n'a pas été trouvé. Chemin recherché: /view/commande-view.fxml");
            }
            
            System.out.println("URL du fichier FXML trouvée: " + fxmlUrl);
            
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            stage.setTitle("Gestion des Commandes");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Pour voir l'erreur complète dans la console
            afficherErreur("Erreur lors de l'ouverture de la vue des commandes", e);
        }
    }

    @FXML
    private void openLivreursView() {
        try {
            Stage stage = new Stage();
            new view.LivreurView(stage);
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture de la vue des livreurs", e);
        }
    }

    @FXML
    private void openCarteView() {
        try {
            System.out.println("Ouverture de la vue carte...");
            
            // Charger le FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/carte-view.fxml"));
            System.out.println("FXML chargé avec succès");
            
            // Créer la scène
            Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
            System.out.println("Scène créée avec succès");
            
            // Configurer et afficher la fenêtre
            Stage stage = new Stage();
            stage.setTitle("Suivi des Livreurs");
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
            
            System.out.println("Vue carte affichée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier FXML : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur lors du chargement de la vue carte", e);
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
            afficherErreur("Erreur lors de l'ouverture de la vue carte", e);
        }
    }

    private void afficherErreur(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.setContentText("Détails : " + e.getMessage());
        alert.showAndWait();
    }
}
