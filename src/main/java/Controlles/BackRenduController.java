package Controlles;

import java.io.IOException;
import java.sql.SQLException;

import Entities.Rendu;
import Services.RenduService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class BackRenduController {
    @FXML private Text titleText;
    @FXML private TextField messageField;
    @FXML private TextField typeField;
    @FXML private DatePicker datePicker;
    @FXML private Button actionButton;
    @FXML private Label messageError;
    @FXML private Label typeError;
    @FXML private Label dateError;

    private Rendu renduToUpdate;
    private final RenduService renduService = new RenduService();

    @FXML
    public void initialize() {
        // Clear error messages when fields are focused
        messageField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                messageError.setText("");
                messageField.setStyle("");
            }
        });
        
        typeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                typeError.setText("");
                typeField.setStyle("");
            }
        });
        
        datePicker.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                dateError.setText("");
                datePicker.setStyle("");
            }
        });
    }

    @FXML
    private void handleAction() {
        if (validateInput()) {
            try {
                if (renduToUpdate == null) {
                    Rendu rendu = new Rendu(
                            messageField.getText(),
                            typeField.getText(),
                            datePicker.getValue()
                    );
                    renduService.addRendu(rendu);
                    showAlert("Rendu added successfully!", Alert.AlertType.INFORMATION);
                } else {
                    renduToUpdate.setMessage_rendu(messageField.getText());
                    renduToUpdate.setType_rendu(typeField.getText());
                    renduToUpdate.setDate_envoi_rendu(datePicker.getValue());
                    renduService.updateRendu(renduToUpdate);
                    showAlert("Rendu updated successfully!", Alert.AlertType.INFORMATION);
                }
                clearFields();
            } catch (SQLException e) {
                showAlert("Error saving Rendu: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void showListView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RenduListView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("List Rendu");
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.show();
        } catch (IOException e) {
            showAlert("Error loading list view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setUpdateMode(Rendu rendu) {
        this.renduToUpdate = rendu;
        titleText.setText("Update Rendu");
        actionButton.setText("Update");
        messageField.setText(rendu.getMessage_rendu());
        typeField.setText(rendu.getType_rendu());
        datePicker.setValue(rendu.getDate_envoi_rendu());
    }

    private boolean validateInput() {
        boolean isValid = true;
        String errorStyle = "-fx-border-color: red; -fx-border-width: 2px;";

        if (messageField.getText().isEmpty()) {
            messageError.setText("Message is required");
            messageField.setStyle(errorStyle);
            isValid = false;
        }
        if (typeField.getText().isEmpty()) {
            typeError.setText("Type is required");
            typeField.setStyle(errorStyle);
            isValid = false;
        }
        if (datePicker.getValue() == null) {
            dateError.setText("Date is required");
            datePicker.setStyle(errorStyle);
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        messageField.clear();
        typeField.clear();
        datePicker.setValue(null);
        messageError.setText("");
        typeError.setText("");
        dateError.setText("");
        messageField.setStyle("");
        typeField.setStyle("");
        datePicker.setStyle("");
        renduToUpdate = null;
        titleText.setText("Add Rendu");
        actionButton.setText("Add");
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType == Alert.AlertType.ERROR ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}