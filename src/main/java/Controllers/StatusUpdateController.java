package Controllers;

import Entities.Reclamation;
import Services.ReclamationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

public class StatusUpdateController {
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Button cancelButton;
    @FXML
    private Button updateButton;

    private Reclamation reclamation;
    private BackReclamationController parentController;
    private final ReclamationService reclamationService = new ReclamationService();

    @FXML
    private void initialize() {
        // Initialiser le ComboBox avec les options de statut
        statusComboBox.setItems(FXCollections.observableArrayList(
            "Pending",
            "In Progress",
            "Resolved"
        ));
        
        // Définir le style des éléments de la liste
        statusComboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Pending":
                            setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                            break;
                        case "In Progress":
                            setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                            break;
                        case "Resolved":
                            setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        
        // Définir le style du bouton sélectionné
        statusComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Pending":
                            setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                            break;
                        case "In Progress":
                            setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                            break;
                        case "Resolved":
                            setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        // Sélectionner le statut actuel dans le ComboBox
        statusComboBox.setValue(reclamation.getStatut_rec());
    }

    public void setParentController(BackReclamationController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleUpdate() {
        String newStatus = statusComboBox.getValue();
        if (newStatus != null) {
            updateStatus(newStatus);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void updateStatus(String newStatus) {
        try {
            reclamation.setStatut_rec(newStatus);
            reclamationService.updateReclamation(reclamation);
            parentController.loadReclamationsAsync();
            parentController.showSuccess("Statut mis à jour avec succès");
            closeWindow();
        } catch (Exception e) {
            parentController.showError("Erreur lors de la mise à jour du statut");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
} 