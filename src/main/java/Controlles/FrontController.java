package Controlles;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class FrontController {

    @FXML
    private Button renduButton;

    @FXML
    private Button alerteButton;

    @FXML
    private void handleRenduButton() {
        try {
            System.out.println("Attempting to load Rendu.fxml...");
            URL fxmlUrl = getClass().getResource("/views/Rendu.fxml");
            if (fxmlUrl == null) {
                System.err.println("Rendu.fxml not found! Check the path and file location.");
                return;
            }
            System.out.println("Found Rendu.fxml at: " + fxmlUrl);
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage stage = (Stage) renduButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800); // Set the same dimensions as Front.fxml
            stage.setScene(scene);
            stage.setTitle("GreenGrow - Plant Management");
            stage.show();
            System.out.println("Successfully loaded Rendu.fxml");
        } catch (IOException e) {
            System.err.println("Error loading Rendu.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAlerteButton() {
        try {
            System.out.println("Attempting to load Alerte.fxml...");
            URL fxmlUrl = getClass().getResource("/views/Alerte.fxml");
            if (fxmlUrl == null) {
                System.err.println("Alerte.fxml not found! Check the path and file location.");
                return;
            }
            System.out.println("Found Alerte.fxml at: " + fxmlUrl);
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage stage = (Stage) alerteButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800); // Set the same dimensions as Front.fxml
            stage.setScene(scene);
            stage.setTitle("GreenGrow - Alerts");
            stage.show();
            System.out.println("Successfully loaded Alerte.fxml");
        } catch (IOException e) {
            System.err.println("Error loading Alerte.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}