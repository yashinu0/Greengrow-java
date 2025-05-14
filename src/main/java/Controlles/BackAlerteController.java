package Controlles;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;

import Entities.Alerte;
import Services.AlerteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class BackAlerteController {
    @FXML private Text titleText;
    @FXML private TextField niveauUrgenceField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private Button actionButton;
    @FXML private Label niveauUrgenceError;
    @FXML private Label dateError;
    @FXML private Label timeError;

    private Alerte alerteToUpdate;
    private final AlerteService alerteService = new AlerteService();

    @FXML
    public void initialize() {
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // Clear error messages when fields are focused
        niveauUrgenceField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                niveauUrgenceError.setText("");
                niveauUrgenceField.setStyle("");
            }
        });
        
        datePicker.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                dateError.setText("");
                datePicker.setStyle("");
            }
        });
        
        hourSpinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                timeError.setText("");
                hourSpinner.setStyle("");
                minuteSpinner.setStyle("");
            }
        });
        
        minuteSpinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                timeError.setText("");
                hourSpinner.setStyle("");
                minuteSpinner.setStyle("");
            }
        });
    }

    @FXML
    private void handleAction() {
        if (validateInput()) {
            try {
                LocalDateTime tempsLimite = LocalDateTime.of(
                        datePicker.getValue(),
                        LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
                );

                if (alerteToUpdate == null) {
                    Alerte alerte = new Alerte(
                            niveauUrgenceField.getText(),
                            tempsLimite
                    );
                    alerteService.addAlerte(alerte);
                    showAlert("Alert added successfully!", Alert.AlertType.INFORMATION);
                } else {
                    alerteToUpdate.setNiveau_urgence_alerte(niveauUrgenceField.getText());
                    alerteToUpdate.setTemps_limite_alerte(tempsLimite);
                    alerteService.updateAlerte(alerteToUpdate);
                    showAlert("Alert updated successfully!", Alert.AlertType.INFORMATION);
                }
                clearFields();
            } catch (SQLException e) {
                showAlert("Error saving alert: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void showListView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AlerteListView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Alert List");
            stage.setMinWidth(600);
            stage.setMinHeight(500);
            stage.show();
        } catch (IOException e) {
            showAlert("Error loading list view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setUpdateMode(Alerte alerte) {
        this.alerteToUpdate = alerte;
        titleText.setText("Update Alert");
        actionButton.setText("Update");
        niveauUrgenceField.setText(alerte.getNiveau_urgence_alerte());
        datePicker.setValue(alerte.getTemps_limite_alerte().toLocalDate());
        hourSpinner.getValueFactory().setValue(alerte.getTemps_limite_alerte().getHour());
        minuteSpinner.getValueFactory().setValue(alerte.getTemps_limite_alerte().getMinute());
    }

    private boolean validateInput() {
        boolean isValid = true;
        String errorStyle = "-fx-border-color: red; -fx-border-width: 2px;";

        if (niveauUrgenceField.getText().isEmpty()) {
            niveauUrgenceError.setText("Urgency level is required");
            niveauUrgenceField.setStyle(errorStyle);
            isValid = false;
        }
        if (datePicker.getValue() == null) {
            dateError.setText("Date limit is required");
            datePicker.setStyle(errorStyle);
            isValid = false;
        }
        if (hourSpinner.getValue() == null || minuteSpinner.getValue() == null) {
            timeError.setText("Time limit is required");
            hourSpinner.setStyle(errorStyle);
            minuteSpinner.setStyle(errorStyle);
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        niveauUrgenceField.clear();
        datePicker.setValue(null);
        hourSpinner.getValueFactory().setValue(12);
        minuteSpinner.getValueFactory().setValue(0);
        niveauUrgenceError.setText("");
        dateError.setText("");
        timeError.setText("");
        niveauUrgenceField.setStyle("");
        datePicker.setStyle("");
        hourSpinner.setStyle("");
        minuteSpinner.setStyle("");
        alerteToUpdate = null;
        titleText.setText("Add New Alert");
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