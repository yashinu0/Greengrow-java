package Controllers;

import Entities.Feed;
import Services.FeedService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import java.util.ArrayList;

public class BackFeedController implements Initializable {
    @FXML private VBox feedbacksContainer;
    @FXML private Text allCount;
    @FXML private Text processedCount;
    @FXML private Text unprocessedCount;
    @FXML private VBox allBox;
    @FXML private VBox processedBox;
    @FXML private VBox unprocessedBox;
    @FXML private TextField searchField;

    private final FeedService feedService = new FeedService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private List<Feed> allFeedbacks = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        loadFeedbacksAsync();
    }

    private void setupEventHandlers() {
        // Gestionnaire de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFeedbacks(newValue);
        });

        // Gestionnaires des filtres
        allBox.setOnMouseClicked(event -> {
            updateUI(allFeedbacks);
            updateCounters(allFeedbacks.size(), 
                allFeedbacks.stream().filter(Feed::isIs_processed).count(),
                allFeedbacks.stream().filter(f -> !f.isIs_processed()).count());
        });

        processedBox.setOnMouseClicked(event -> {
            List<Feed> processed = allFeedbacks.stream()
                .filter(Feed::isIs_processed)
                .collect(Collectors.toList());
            updateUI(processed);
            updateCounters(allFeedbacks.size(), processed.size(), 
                allFeedbacks.size() - processed.size());
        });

        unprocessedBox.setOnMouseClicked(event -> {
            List<Feed> unprocessed = allFeedbacks.stream()
                .filter(f -> !f.isIs_processed())
                .collect(Collectors.toList());
            updateUI(unprocessed);
            updateCounters(allFeedbacks.size(), 
                allFeedbacks.size() - unprocessed.size(), unprocessed.size());
        });
    }

    private void filterFeedbacks(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updateUI(allFeedbacks);
            return;
        }

        String lowerSearch = searchText.toLowerCase();
        List<Feed> filtered = allFeedbacks.stream()
            .filter(feed -> 
                feed.getName_feed().toLowerCase().contains(lowerSearch) ||
                feed.getEmail_feed().toLowerCase().contains(lowerSearch) ||
                feed.getSubject_feed().toLowerCase().contains(lowerSearch) ||
                feed.getCommentaire_feed().toLowerCase().contains(lowerSearch))
            .collect(Collectors.toList());

        updateUI(filtered);
    }

    private void loadFeedbacksAsync() {
        Task<List<Feed>> loadTask = new Task<List<Feed>>() {
            @Override
            protected List<Feed> call() throws Exception {
                return feedService.getAllFeeds();
            }
        };

        loadTask.setOnSucceeded(event -> {
            final List<Feed> feedbacks = loadTask.getValue();
            allFeedbacks = feedbacks;
            updateUI(feedbacks);
        });

        loadTask.setOnFailed(event -> {
            showError("Erreur lors du chargement des feedbacks");
            loadTask.getException().printStackTrace();
        });

        executor.submit(loadTask);
    }

    private void updateUI(List<Feed> feedbacks) {
        Platform.runLater(() -> {
            feedbacksContainer.getChildren().clear();
            feedbacks.forEach(feed -> feedbacksContainer.getChildren().add(createFeedbackCard(feed)));
            updateCounters(allFeedbacks.size(), 
                allFeedbacks.stream().filter(Feed::isIs_processed).count(),
                allFeedbacks.stream().filter(f -> !f.isIs_processed()).count());
        });
    }

    private void updateCounters(long total, long processed, long unprocessed) {
        allCount.setText(String.valueOf(total));
        processedCount.setText(String.valueOf(processed));
        unprocessedCount.setText(String.valueOf(unprocessed));
    }

    private void updateFeedbacksDisplay(List<Feed> feedbacks) {
        feedbacksContainer.getChildren().clear();
        
        for (Feed feed : feedbacks) {
            VBox card = createFeedbackCard(feed);
            feedbacksContainer.getChildren().add(card);
        }
    }

    private VBox createFeedbackCard(Feed feed) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMaxWidth(Double.MAX_VALUE);

        // En-tête de la carte
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text name = new Text(feed.getName_feed());
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        Text email = new Text(feed.getEmail_feed());
        email.setStyle("-fx-font-size: 14; -fx-fill: #6c757d;");
        
        Text date = new Text(DATE_FORMAT.format(feed.getDate_feed()));
        date.setStyle("-fx-font-size: 14; -fx-fill: #6c757d;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label status = new Label(feed.isIs_processed() ? "Traité" : "Non traité");
        status.getStyleClass().add(feed.isIs_processed() ? "status-processed" : "status-unprocessed");
        
        header.getChildren().addAll(name, email, date, spacer, status);
        
        // Corps de la carte
        Text subject = new Text("Sujet: " + feed.getSubject_feed());
        subject.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        
        TextArea message = new TextArea(feed.getCommentaire_feed());
        message.setEditable(false);
        message.setWrapText(true);
        message.setPrefRowCount(3);
        message.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button viewButton = new Button("Voir");
        viewButton.getStyleClass().addAll("action-button", "view-button");
        viewButton.setOnAction(e -> showFeedbackDetails(feed));

        Button toggleButton = new Button(feed.isIs_processed() ? "Marquer non traité" : "Marquer traité");
        toggleButton.getStyleClass().addAll("action-button", "toggle-button");
        toggleButton.setOnAction(e -> toggleProcessedStatus(feed));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("action-button", "delete-button");
        deleteButton.setOnAction(e -> handleDelete(feed));

        actions.getChildren().addAll(viewButton, toggleButton, deleteButton);
        
        card.getChildren().addAll(header, subject, message, actions);
        return card;
    }

    private void showFeedbackDetails(Feed feed) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/FeedDetails.fxml"));
            Parent root = loader.load();
            
            FeedDetailsController controller = loader.getController();
            controller.setFeed(feed);
            
            Stage stage = new Stage();
            stage.setTitle("Détails du feedback");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture des détails");
        }
    }

    private void toggleProcessedStatus(Feed feed) {
        try {
            feed.setIs_processed(!feed.isIs_processed());
            feedService.updateFeed(feed);
            loadFeedbacksAsync();
            showSuccess("Statut mis à jour avec succès");
        } catch (Exception e) {
            showError("Erreur lors de la mise à jour du statut");
        }
    }

    private void handleDelete(Feed feed) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce feedback?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                feedService.deleteFeed(feed.getId());
                loadFeedbacksAsync();
                showSuccess("Feedback supprimé avec succès");
            } catch (Exception e) {
                showError("Erreur lors de la suppression du feedback");
            }
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 