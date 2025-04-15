package Controllers;

import Entities.Alerte;
import Services.AlerteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class AlerteListController {
    @FXML private ListView<HBox> alerteListView;

    private final AlerteService alerteService = new AlerteService();
    private final ObservableList<Alerte> alertes = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        loadAlertes();
    }

    private void loadAlertes() {
        try {
            alertes.setAll(alerteService.getAllAlertes());
            alerteListView.getItems().clear();

            for (Alerte alerte : alertes) {
                HBox itemBox = createAlerteItem(alerte);
                alerteListView.getItems().add(itemBox);
            }
        } catch (SQLException e) {
            showAlert("Error loading alerts: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private HBox createAlerteItem(Alerte alerte) {
        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(10));
        itemBox.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5;");

        VBox infoBox = new VBox(5);
        Text idText = new Text("ID: " + alerte.getId_alerte());
        idText.setStyle("-fx-font-weight: bold;");

        Text urgencyText = new Text("Urgency: " + alerte.getNiveau_urgence_alerte());
        urgencyText.setStyle(alerte.getNiveau_urgence_alerte().toLowerCase().contains("high")
                ? "-fx-fill: #d32f2f; -fx-font-weight: bold;"
                : "-fx-fill: #1976d2;");

        Text timeText = new Text("Time: " + alerte.getTemps_limite_alerte().format(timeFormatter));

        infoBox.getChildren().addAll(idText, urgencyText, timeText);

        HBox buttonBox = new HBox(10);
        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> showUpdateView(alerte));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteAlerte(alerte));

        buttonBox.getChildren().addAll(updateBtn, deleteBtn);

        itemBox.getChildren().addAll(infoBox, buttonBox);
        return itemBox;
    }

    private void showUpdateView(Alerte alerte) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BackAlerte.fxml"));
            Parent root = loader.load();

            BackAlerteController controller = loader.getController();
            controller.setUpdateMode(alerte);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update Alert");
            stage.setMinWidth(400);
            stage.setMinHeight(400);
            stage.show();

            ((Stage) alerteListView.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert("Error loading update view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteAlerte(Alerte alerte) {
        try {
            alerteService.deleteAlerte(alerte.getId_alerte());
            loadAlertes();
            showAlert("Alert deleted successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error deleting alert: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BackAlerte.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Alert");
            stage.setMinWidth(400);
            stage.setMinHeight(400);
            stage.show();

            ((Stage) alerteListView.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert("Error returning to add view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType == Alert.AlertType.ERROR ? "Error" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}