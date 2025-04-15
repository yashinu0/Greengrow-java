package Controllers;

import Entities.Reclamation;
import Services.ReclamationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.net.URL;
import java.util.ResourceBundle;

public class ReclamationDetailsController implements Initializable {
    @FXML private Label clientIdLabel;
    @FXML private Label clientEmailLabel;
    @FXML private Label clientRegistrationDateLabel;
    
    @FXML private Label productIdLabel;
    @FXML private Label productNameLabel;
    @FXML private Label productPriceLabel;
    @FXML private Label productAvailabilityLabel;
    
    @FXML private Label reclamationDateLabel;
    @FXML private Label reclamationTypeLabel;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button updateStatusButton;
    @FXML private TextArea messageArea;
    @FXML private Button backButton1;

    private final ReclamationService reclamationService = new ReclamationService();
    private Reclamation currentReclamation;
    private Stage stage;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStatusComboBox();
        setupBackButton();
        setupUpdateButton();
    }

    private void setupStatusComboBox() {
        statusComboBox.setItems(FXCollections.observableArrayList("Pending", "In Progress", "Resolved"));
        statusComboBox.setValue(currentReclamation != null ? currentReclamation.getStatut_rec() : "Pending");
    }

    private void setupBackButton() {
        backButton1.setOnAction(event -> handleBackToList());
    }

    private void setupUpdateButton() {
        updateStatusButton.setOnAction(event -> handleUpdateStatus());
    }

    public void setReclamation(Reclamation reclamation) {
        this.currentReclamation = reclamation;
        updateDisplay();
    }

    private void updateDisplay() {
        if (currentReclamation != null) {
            // Update client information
            clientIdLabel.setText(String.valueOf(currentReclamation.getUtilisateur_id()));
            clientEmailLabel.setText(currentReclamation.getUser_name());
            
            // Update product information
            productIdLabel.setText(String.valueOf(currentReclamation.getProduit_id()));
            
            // Update reclamation information
            reclamationDateLabel.setText(dateFormatter.format(currentReclamation.getDate_rec()));
            reclamationTypeLabel.setText(currentReclamation.getDescription_rec());
            statusLabel.setText(currentReclamation.getStatut_rec());
            messageArea.setText(currentReclamation.getMessage_reclamation());
            
            // Update status combo box
            statusComboBox.setValue(currentReclamation.getStatut_rec());
            
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
        Stage stage = (Stage) backButton1.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleUpdateStatus() {
        if (currentReclamation != null && statusComboBox.getValue() != null) {
            String newStatus = statusComboBox.getValue();
            reclamationService.updateReclamationStatus(currentReclamation.getId(), newStatus);
            currentReclamation.setStatut_rec(newStatus);
            updateDisplay();
            
            // Show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Le statut a été mis à jour avec succès.");
            alert.showAndWait();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
} 