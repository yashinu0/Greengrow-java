package Controlles;

import Entities.Feed;
import Services.FeedService;
import Services.FeedbackAnalysisService;
import Services.FeedbackAnalysisService.SentimentType;
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
    private final FeedbackAnalysisService analysisService = new FeedbackAnalysisService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private List<Feed> allFeedbacks = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        loadFeedbacksAsync();
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Rafraîchissement des feedbacks...");
        loadFeedbacksAsync();
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFeedbacks(newValue);
        });

        allBox.setOnMouseClicked(event -> updateUI(allFeedbacks));
        processedBox.setOnMouseClicked(event -> {
            List<Feed> filtered = allFeedbacks.stream()
                .filter(f -> f.isIs_processed())
                .collect(Collectors.toList());
            updateUI(filtered);
        });
        unprocessedBox.setOnMouseClicked(event -> {
            List<Feed> filtered = allFeedbacks.stream()
                .filter(f -> !f.isIs_processed())
                .collect(Collectors.toList());
            updateUI(filtered);
        });
    }

    private void loadFeedbacksAsync() {
        Task<List<Feed>> task = new Task<>() {
            @Override
            protected List<Feed> call() {
                return feedService.getAllFeeds();
            }
        };

        task.setOnSucceeded(event -> {
            allFeedbacks = task.getValue();
            updateUI(allFeedbacks);
        });

        task.setOnFailed(event -> {
            showError("Erreur lors du chargement des feedbacks");
        });

        executor.submit(task);
    }

    private void updateUI(List<Feed> feedbacks) {
        Platform.runLater(() -> {
            feedbacksContainer.getChildren().clear();
            
            for (Feed feedback : feedbacks) {
                VBox card = createFeedbackCard(feedback);
                feedbacksContainer.getChildren().add(card);
            }

            updateCounters(feedbacks);
        });
    }

    private VBox createFeedbackCard(Feed feedback) {
        VBox card = new VBox(10);
        card.getStyleClass().add("feedback-card");
        card.setPadding(new Insets(15));
        card.setMaxWidth(Double.MAX_VALUE);

        // En-tête avec nom et actions
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text name = new Text(feedback.getName_feed());
        name.getStyleClass().add("feedback-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewButton = new Button("Voir");
        viewButton.getStyleClass().add("action-button");
        viewButton.setOnAction(event -> showFeedbackDetails(feedback));

        Button processButton = new Button(feedback.isIs_processed() ? "Marquer comme non traité" : "Marquer comme traité");
        processButton.getStyleClass().add("action-button");
        processButton.setOnAction(event -> handleProcessAction(feedback));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("action-button");
        deleteButton.setOnAction(event -> handleDeleteAction(feedback));

        HBox actions = new HBox(10);
        actions.getChildren().addAll(viewButton, processButton, deleteButton);

        header.getChildren().addAll(name, spacer, actions);

        // Contenu principal
        Text subject = new Text(feedback.getSubject_feed());
        subject.getStyleClass().add("feedback-subject");

        Text message = new Text(feedback.getCommentaire_feed());
        message.getStyleClass().add("feedback-message");
        message.setWrappingWidth(800);

        // Pied de page avec date et statut
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        
        Text date = new Text(DATE_FORMAT.format(feedback.getDate_feed()));
        date.getStyleClass().add("feedback-date");

        Label statusLabel = new Label(feedback.isIs_processed() ? "Traité" : "Non traité");
        statusLabel.getStyleClass().add(feedback.isIs_processed() ? "status-processed" : "status-unprocessed");

        // Gestion sécurisée de la conversion du sentiment
        SentimentType sentiment;
        try {
            sentiment = SentimentType.valueOf(feedback.getSentiment().toUpperCase());
        } catch (IllegalArgumentException e) {
            sentiment = SentimentType.NEUTRAL;
        }
        Label sentimentLabel = new Label(getSentimentText(sentiment));
        sentimentLabel.getStyleClass().add(getSentimentStyleClass(sentiment));

        footer.getChildren().addAll(date, statusLabel, sentimentLabel);

        // Assemblage final de la carte
        card.getChildren().addAll(header, subject, message, footer);
        return card;
    }

    private String getSentimentText(SentimentType sentiment) {
        switch (sentiment) {
            case POSITIVE:
                return "Positif";
            case NEGATIVE:
                return "Négatif";
            default:
                return "Neutre";
        }
    }

    private String getSentimentStyleClass(SentimentType sentiment) {
        switch (sentiment) {
            case POSITIVE:
                return "sentiment-positive";
            case NEGATIVE:
                return "sentiment-negative";
            default:
                return "sentiment-neutral";
        }
    }

    private void updateCounters(List<Feed> feedbacks) {
        long total = feedbacks.size();
        long processed = feedbacks.stream().filter(Feed::isIs_processed).count();
        long unprocessed = total - processed;

        allCount.setText(String.valueOf(total));
        processedCount.setText(String.valueOf(processed));
        unprocessedCount.setText(String.valueOf(unprocessed));
    }

    private void filterFeedbacks(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updateUI(allFeedbacks);
            return;
        }

        String searchLower = searchText.toLowerCase();
        List<Feed> filtered = allFeedbacks.stream()
                .filter(feedback -> {
                    // Vérification du nom
                    String name = feedback.getName_feed();
                    boolean nameMatch = name != null && name.toLowerCase().contains(searchLower);
                    
                    // Vérification du sujet
                    String subject = feedback.getSubject_feed();
                    boolean subjectMatch = subject != null && subject.toLowerCase().contains(searchLower);
                    
                    // Vérification du commentaire
                    String comment = feedback.getCommentaire_feed();
                    boolean commentMatch = comment != null && comment.toLowerCase().contains(searchLower);
                    
                    // Vérification de la date
                    String formattedDate = DATE_FORMAT.format(feedback.getDate_feed());
                    boolean dateMatch = formattedDate.contains(searchLower);
                    
                    // Vérification du statut
                    String status = feedback.isIs_processed() ? "Traité" : "Non traité";
                    boolean statusMatch = status.toLowerCase().contains(searchLower);
                    
                    // Vérification du sentiment
                    SentimentType sentiment;
                    try {
                        sentiment = SentimentType.valueOf(feedback.getSentiment().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sentiment = SentimentType.NEUTRAL;
                    }
                    String sentimentText = getSentimentText(sentiment);
                    boolean sentimentMatch = sentimentText.toLowerCase().contains(searchLower);
                    
                    return nameMatch || subjectMatch || commentMatch || dateMatch || 
                           statusMatch || sentimentMatch;
                })
                .collect(Collectors.toList());

        updateUI(filtered);
    }

    private void showFeedbackDetails(Feed feedback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/FeedDetails.fxml"));
            Parent root = loader.load();
            
            FeedDetailsController controller = loader.getController();
            controller.setFeed(feedback);
            
            Stage stage = new Stage();
            stage.setTitle("Détails du feedback");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture des détails");
        }
    }

    private void handleProcessAction(Feed feedback) {
        feedback.setIs_processed(!feedback.isIs_processed());
        feedService.updateFeed(feedback);
        loadFeedbacksAsync();
    }

    private void handleDeleteAction(Feed feedback) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce feedback ?");

        ButtonType buttonTypeYes = new ButtonType("Oui", ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("Non", ButtonData.NO);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(type -> {
            if (type == buttonTypeYes) {
                feedService.deleteFeed(feedback.getId());
                loadFeedbacksAsync();
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 