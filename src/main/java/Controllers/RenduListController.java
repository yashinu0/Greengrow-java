package Controllers;

import Entities.Rendu;
import Services.RenduService;
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

public class RenduListController {
    @FXML private ListView<HBox> renduListView;

    private final RenduService renduService = new RenduService();
    private final ObservableList<Rendu> rendus = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadRendus();
    }

    private void loadRendus() {
        try {
            rendus.setAll(renduService.getAllRendus());
            renduListView.getItems().clear();

            for (Rendu rendu : rendus) {
                HBox itemBox = createRenduItem(rendu);
                renduListView.getItems().add(itemBox);
            }
        } catch (SQLException e) {
            showAlert("Error loading Rendu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private HBox createRenduItem(Rendu rendu) {
        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setPadding(new Insets(10));
        itemBox.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5;");

        VBox infoBox = new VBox(5);
        Text idText = new Text("ID: " + rendu.getId_rendu());
        idText.setStyle("-fx-font-weight: bold;");

        Text messageText = new Text("Message: " + rendu.getMessage_rendu());
        messageText.setWrappingWidth(400);

        HBox detailsBox = new HBox(20);
        Text typeText = new Text("Type: " + rendu.getType_rendu());
        Text dateText = new Text("Date: " + rendu.getDate_envoi_rendu());
        detailsBox.getChildren().addAll(typeText, dateText);

        infoBox.getChildren().addAll(idText, messageText, detailsBox);

        HBox buttonBox = new HBox(10);
        Button updateBtn = new Button("Update");
        updateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> showUpdateView(rendu));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteRendu(rendu));

        buttonBox.getChildren().addAll(updateBtn, deleteBtn);

        itemBox.getChildren().addAll(infoBox, buttonBox);
        return itemBox;
    }

    private void showUpdateView(Rendu rendu) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BackRendu.fxml"));
            Parent root = loader.load();

            BackRenduController controller = loader.getController();
            controller.setUpdateMode(rendu);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Update Rendu");
            stage.setMinWidth(400);
            stage.setMinHeight(400);
            stage.show();

            ((Stage) renduListView.getScene().getWindow()).close();
        } catch (IOException e) {
            showAlert("Error loading update view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deleteRendu(Rendu rendu) {
        try {
            renduService.deleteRendu(rendu.getId_rendu());
            loadRendus();
            showAlert("Rendu deleted successfully!", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error deleting Rendu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BackRendu.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Rendu");
            stage.setMinWidth(400);
            stage.setMinHeight(400);
            stage.show();

            ((Stage) renduListView.getScene().getWindow()).close();
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