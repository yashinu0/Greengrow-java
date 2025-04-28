package Controlles;

import Entities.Feed;
import Services.FeedService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;
import java.text.SimpleDateFormat;
import java.util.Map;

public class FeedDetailsController implements Initializable {
    @FXML private Text nameLabel;
    @FXML private Button statusButton;
    @FXML private TableView<FeedInfo> infoTable;
    @FXML private TableColumn<FeedInfo, String> fieldColumn;
    @FXML private TableColumn<FeedInfo, String> valueColumn;
    @FXML private TextArea messageArea;

    private Feed feed;
    private final FeedService feedService = new FeedService();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
        updateUI();
    }

    private void setupTable() {
        fieldColumn.setCellValueFactory(new PropertyValueFactory<>("field"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    }

    private void updateUI() {
        if (feed == null) return;

        nameLabel.setText(feed.getName_feed());
        statusButton.setText(feed.isIs_processed() ? "Marquer comme non traité" : "Marquer comme traité");
        messageArea.setText(feed.getCommentaire_feed());

        // Update info table
        infoTable.getItems().clear();
        infoTable.getItems().add(new FeedInfo("Email", feed.getEmail_feed()));
        infoTable.getItems().add(new FeedInfo("Date", DATE_FORMAT.format(feed.getDate_feed())));
        infoTable.getItems().add(new FeedInfo("Sujet", feed.getSubject_feed()));
        infoTable.getItems().add(new FeedInfo("Statut", feed.isIs_processed() ? "Traité" : "Non traité"));
    }

    @FXML
    private void handleStatusChange() {
        feed.setIs_processed(!feed.isIs_processed());
        feedService.updateFeed(feed);
        updateUI();
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce feedback ?");

        ButtonType buttonTypeYes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("Non", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(type -> {
            if (type == buttonTypeYes) {
                feedService.deleteFeed(feed.getId());
                closeWindow();
            }
        });
    }

    @FXML
    private void handleBackToList() {
        closeWindow();
    }

    @FXML
    private void handleReplyEmail() {
        try {
            // Récupérer l'email depuis la table
            String emailAddress = null;
            for (FeedInfo info : infoTable.getItems()) {
                if (info.getField().equals("Email")) {
                    emailAddress = info.getValue();
                    break;
                }
            }
            
            if (emailAddress == null || emailAddress.isEmpty()) {
                throw new Exception("Adresse email non trouvée");
            }
            
            // Créer l'URI mailto
            String mailtoUri = String.format("mailto:%s", emailAddress.replace(" ", "%20"));
            
            // Ouvrir l'application mail par défaut
            java.awt.Desktop.getDesktop().mail(new java.net.URI(mailtoUri));
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de l'application mail : " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir l'application mail : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }

    // Helper class for table data
    public static class FeedInfo {
        private final String field;
        private final String value;

        public FeedInfo(String field, String value) {
            this.field = field;
            this.value = value;
        }

        public String getField() {
            return field;
        }

        public String getValue() {
            return value;
        }
    }
} 