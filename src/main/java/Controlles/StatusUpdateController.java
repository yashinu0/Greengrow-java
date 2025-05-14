package Controlles;

import Entities.Reclamation;
import Services.ReclamationService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class StatusUpdateController implements Initializable {
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button updateButton;
    @FXML private Button cancelButton;

    private final ReclamationService reclamationService = new ReclamationService();
    private Reclamation currentReclamation;
    private BackReclamationController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStatusComboBox();
        setupButtons();
    }

    private void setupStatusComboBox() {
        statusComboBox.getItems().addAll("Pending", "In Progress", "Resolved");
    }

    private void setupButtons() {
        updateButton.setOnAction(event -> handleUpdate());
        cancelButton.setOnAction(event -> handleCancel());
    }

    public void setReclamation(Reclamation reclamation) {
        this.currentReclamation = reclamation;
        if (reclamation != null) {
            statusComboBox.setValue(reclamation.getStatut_rec());
        }
    }

    public void setParentController(BackReclamationController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleUpdate() {
        if (currentReclamation != null) {
            String newStatus = statusComboBox.getValue();
            if (newStatus != null && !newStatus.equals(currentReclamation.getStatut_rec())) {
                currentReclamation.setStatut_rec(newStatus);
                reclamationService.updateReclamation(currentReclamation);
                if (parentController != null) {
                    parentController.loadReclamationsAsync();
                }
                closeWindow();
            }
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
} 