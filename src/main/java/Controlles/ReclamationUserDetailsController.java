package Controlles;

import Entities.Reclamation;
import Services.ReclamationService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import java.io.IOException;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class ReclamationUserDetailsController implements Initializable {
    @FXML private Label typeLabel;
    @FXML private Label dateLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea messageArea;
    @FXML private Button backButton;
    @FXML private Button openChatButton;
    @FXML private Button chatBotButton;
    
    private final ReclamationService reclamationService = new ReclamationService();
    private Reclamation currentReclamation;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBackButton();
    }

    private void setupBackButton() {
        backButton.setOnAction(event -> handleBackToList());
    }

    public void setReclamation(Reclamation reclamation) {
        this.currentReclamation = reclamation;
        updateDisplay();
    }

    private void updateDisplay() {
        if (currentReclamation != null) {
            typeLabel.setText(currentReclamation.getDescription_rec());
            dateLabel.setText(dateFormatter.format(currentReclamation.getDate_rec()));
            statusLabel.setText(currentReclamation.getStatut_rec());
            messageArea.setText(currentReclamation.getMessage_reclamation());
            
            // Style the status label based on the current status
            String baseStyle = "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 13px;";
            switch (currentReclamation.getStatut_rec()) {
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
        }
    }

    @FXML
    private void handleBackToList() {
        try {
            // Load the user reclamations list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ReclamationUser.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the user ID
            ReclamationUserController controller = loader.getController();
            controller.setCurrentUserId(currentReclamation.getUtilisateur_id());
            
            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();
            
            // Set the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Réclamations");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ChatView.fxml"));
            Parent root = loader.load();
            
            ChatViewController controller = loader.getController();
            controller.setReclamationId(currentReclamation.getId());
            controller.setUserId(currentReclamation.getUtilisateur_id());
            
            Stage chatStage = new Stage();
            chatStage.setTitle("Discussion - Réclamation #" + currentReclamation.getId());
            chatStage.setScene(new Scene(root));
            chatStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openChatBot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ChatBotView.fxml"));
            Parent root = loader.load();
            ChatBotController controller = loader.getController();
            controller.setReclamationId(currentReclamation.getId());
            Stage chatStage = new Stage();
            chatStage.setTitle("Chatbot - Assistance");
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 