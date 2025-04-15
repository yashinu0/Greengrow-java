package Controllers;

import Entities.Feed;
import Services.FeedService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;

public class FeedDetailsController implements Initializable {
    @FXML private Text nameLabel;
    @FXML private Button statusButton;
    @FXML private TableView<InfoRow> infoTable;
    @FXML private TableColumn<InfoRow, String> fieldColumn;
    @FXML private TableColumn<InfoRow, String> valueColumn;
    @FXML private TextArea messageArea;
    @FXML private Button deleteButton;

    private Feed currentFeed;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final FeedService feedService = new FeedService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
    }

    private void setupTable() {
        fieldColumn.setCellValueFactory(cellData -> cellData.getValue().fieldProperty());
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
    }

    public void setFeed(Feed feed) {
        this.currentFeed = feed;
        updateUI();
    }

    private void updateUI() {
        if (currentFeed == null) return;

        // Update name label
        nameLabel.setText(currentFeed.getName_feed());

        // Update status button
        updateStatusButton();

        // Update info table
        List<InfoRow> infoRows = new ArrayList<>();
        infoRows.add(new InfoRow("ID", String.valueOf(currentFeed.getId())));
        infoRows.add(new InfoRow("Nom", currentFeed.getName_feed()));
        infoRows.add(new InfoRow("Email", currentFeed.getEmail_feed()));
        infoRows.add(new InfoRow("Sujet", currentFeed.getSubject_feed()));
        infoRows.add(new InfoRow("Date", dateFormatter.format(currentFeed.getDate_feed())));
        
        infoTable.getItems().setAll(infoRows);

        // Update message area
        messageArea.setText(currentFeed.getCommentaire_feed());
    }

    private void updateStatusButton() {
        if (currentFeed.isIs_processed()) {
            statusButton.setText("Déja traité");
            statusButton.getStyleClass().add("success-button");
            statusButton.getStyleClass().remove("warning-button");
        } else {
            statusButton.setText("Marquer comme traité");
            statusButton.getStyleClass().add("warning-button");
            statusButton.getStyleClass().remove("success-button");
        }
    }

    @FXML
    private void handleStatusChange() {
        if (currentFeed != null) {
            currentFeed.setIs_processed(!currentFeed.isIs_processed());
            feedService.updateFeed(currentFeed);
            updateStatusButton();
            showSuccess("Statut mis à jour avec succès");
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce message?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                feedService.deleteFeed(currentFeed.getId());
                showSuccess("Message supprimé avec succès");
                handleBackToList();
            } catch (Exception e) {
                showError("Erreur", "Impossible de supprimer le message: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBackToList() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleReplyEmail() {
        // TODO: Implémenter le service de mailing
        showSuccess("Fonctionnalité de réponse par email à venir");
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Helper class for table data
    public static class InfoRow {
        private final javafx.beans.property.SimpleStringProperty field;
        private final javafx.beans.property.SimpleStringProperty value;

        InfoRow(String field, String value) {
            this.field = new javafx.beans.property.SimpleStringProperty(field);
            this.value = new javafx.beans.property.SimpleStringProperty(value);
        }

        public javafx.beans.property.StringProperty fieldProperty() {
            return field;
        }

        public javafx.beans.property.StringProperty valueProperty() {
            return value;
        }
    }
} 