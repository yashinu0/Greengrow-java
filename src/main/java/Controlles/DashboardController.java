package Controlles;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class DashboardController {
    @FXML private VBox content;
    @FXML private VBox ProduitMenu;

    // Ajoutez cette méthode manquante

    @FXML
    private void handleProduit(ActionEvent event) {
        loadFXMLWithAnimation("/produit.fxml");
    }

    @FXML
    private void handleCategroy(ActionEvent event) {
        loadFXMLWithAnimation("/category_view.fxml");
    }
    @FXML
    private void handleUser(ActionEvent event) {
        loadFXMLWithAnimation("/ListUser.fxml");
    }
    @FXML
    private void handlefront(ActionEvent event) {
        loadFXMLWithAnimation("/FrontEnd.fxml");
    }

    @FXML
    private void toggleProduitMenu(ActionEvent event) {
        ProduitMenu.setVisible(!ProduitMenu.isVisible());
    }


    private void loadFXMLWithAnimation(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("Fichier FXML introuvable: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);

            content.getChildren().setAll(root);
            ft.play();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de chargement",
                    "Impossible de charger la vue: " + fxmlPath,
                    e.getMessage());
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}