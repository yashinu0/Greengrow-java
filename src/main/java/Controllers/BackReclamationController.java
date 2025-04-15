package Controllers;

import Entities.Reclamation;
import Services.ReclamationService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.io.IOException;

public class BackReclamationController implements Initializable {
    @FXML
    private VBox reclamationsContainer;
    @FXML
    private VBox allBox;
    @FXML
    private VBox pendingBox;
    @FXML
    private VBox inProgressBox;
    @FXML
    private VBox resolvedBox;
    @FXML
    private TextField searchField;
    @FXML
    private Text allCount;
    @FXML
    private Text pendingCount;
    @FXML
    private Text inProgressCount;
    @FXML
    private Text resolvedCount;

    private final ReclamationService reclamationService = new ReclamationService();
    private List<Reclamation> allReclamations = new ArrayList<>();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        loadReclamationsAsync();
    }

    private void setupEventHandlers() {
        // Gestionnaire de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReclamations(newValue);
        });

        // Gestionnaires des filtres
        allBox.setOnMouseClicked(event -> {
            updateUI(allReclamations);
            updateCounters(allReclamations.size(), 
                allReclamations.stream().filter(r -> r.getStatut_rec().equals("Pending")).count(),
                allReclamations.stream().filter(r -> r.getStatut_rec().equals("In Progress")).count(),
                allReclamations.stream().filter(r -> r.getStatut_rec().equals("Resolved")).count());
        });

        pendingBox.setOnMouseClicked(event -> {
            List<Reclamation> pending = allReclamations.stream()
                .filter(r -> r.getStatut_rec().equals("Pending"))
                .collect(Collectors.toList());
            updateUI(pending);
            updateCounters(allReclamations.size(), pending.size(), 0, 0);
        });

        inProgressBox.setOnMouseClicked(event -> {
            List<Reclamation> inProgress = allReclamations.stream()
                .filter(r -> r.getStatut_rec().equals("In Progress"))
                .collect(Collectors.toList());
            updateUI(inProgress);
            updateCounters(allReclamations.size(), 0, inProgress.size(), 0);
        });

        resolvedBox.setOnMouseClicked(event -> {
            List<Reclamation> resolved = allReclamations.stream()
                .filter(r -> r.getStatut_rec().equals("Resolved"))
                .collect(Collectors.toList());
            updateUI(resolved);
            updateCounters(allReclamations.size(), 0, 0, resolved.size());
        });
    }

    private void filterReclamations(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updateUI(allReclamations);
            return;
        }

        String lowerSearch = searchText.toLowerCase();
        List<Reclamation> filtered = allReclamations.stream()
            .filter(reclamation -> 
                reclamation.getUser_name().toLowerCase().contains(lowerSearch) ||
                reclamation.getDescription_rec().toLowerCase().contains(lowerSearch) ||
                reclamation.getMessage_reclamation().toLowerCase().contains(lowerSearch))
            .collect(Collectors.toList());

        updateUI(filtered);
    }

    public void loadReclamationsAsync() {
        Task<List<Reclamation>> loadTask = new Task<List<Reclamation>>() {
            @Override
            protected List<Reclamation> call() throws Exception {
                return reclamationService.getAllReclamations();
            }
        };

        loadTask.setOnSucceeded(event -> {
            allReclamations = loadTask.getValue();
            updateUI(allReclamations);
        });

        loadTask.setOnFailed(event -> {
            showError("Erreur lors du chargement des réclamations");
            loadTask.getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }

    private void updateUI(List<Reclamation> reclamations) {
        Platform.runLater(() -> {
            reclamationsContainer.getChildren().clear();
            for (Reclamation reclamation : reclamations) {
                addReclamationCard(reclamation);
            }
            updateCounters(allReclamations.size(), 
                allReclamations.stream().filter(r -> r.getStatut_rec().equals("Pending")).count(),
                allReclamations.stream().filter(r -> r.getStatut_rec().equals("In Progress")).count(),
                allReclamations.stream().filter(r -> r.getStatut_rec().equals("Resolved")).count());
        });
    }

    private void addReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setMaxWidth(Double.MAX_VALUE);

        // En-tête de la carte
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Text name = new Text(reclamation.getUser_name());
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        Text date = new Text(DATE_FORMAT.format(reclamation.getDate_rec()));
        date.setStyle("-fx-font-size: 14; -fx-fill: #6c757d;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label status = new Label(reclamation.getStatut_rec());
        status.getStyleClass().add("status-" + reclamation.getStatut_rec().toLowerCase().replace(" ", "-"));
        
        header.getChildren().addAll(name, date, spacer, status);
        
        // Corps de la carte
        Text subject = new Text("Sujet: " + reclamation.getDescription_rec());
        subject.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        
        TextArea message = new TextArea(reclamation.getMessage_reclamation());
        message.setEditable(false);
        message.setWrapText(true);
        message.setPrefRowCount(3);
        message.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        // Actions
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewButton = new Button("Voir");
        viewButton.getStyleClass().addAll("action-button", "view-button");
        viewButton.setOnAction(event -> showReclamationDetails(reclamation));
        
        Button processButton = new Button("Traiter");
        processButton.getStyleClass().addAll("action-button", "toggle-button");
        processButton.setOnAction(event -> handleProcessAction(reclamation));
        
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("action-button", "delete-button");
        deleteButton.setOnAction(event -> handleDeleteAction(reclamation));
        
        actions.getChildren().addAll(viewButton, processButton, deleteButton);
        
        card.getChildren().addAll(header, subject, message, actions);
        reclamationsContainer.getChildren().add(card);
    }

    private Color getStatusColor(String status) {
        switch (status) {
            case "Pending":
                return Color.RED;
            case "In Progress":
                return Color.ORANGE;
            case "Resolved":
                return Color.GREEN;
            default:
                return Color.BLACK;
        }
    }

    private void updateCounters(int total, long pending, long inProgress, long resolved) {
        Platform.runLater(() -> {
            allCount.setText(String.valueOf(total));
            pendingCount.setText(String.valueOf(pending));
            inProgressCount.setText(String.valueOf(inProgress));
            resolvedCount.setText(String.valueOf(resolved));
        });
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showReclamationDetails(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ReclamationDetails.fxml"));
            Parent root = loader.load();
            
            ReclamationDetailsController controller = loader.getController();
            controller.setReclamation(reclamation);
            
            Stage stage = new Stage();
            stage.setTitle("Détails de la réclamation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture des détails");
        }
    }

    private void handleProcessAction(Reclamation reclamation) {
        try {
            // Charger le FXML de la fenêtre de statut
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/StatusUpdate.fxml"));
            Parent root = loader.load();
            
            // Créer une nouvelle scène
            Scene scene = new Scene(root);
            
            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("Mettre à jour le statut");
            stage.setScene(scene);
            
            // Empêcher la fermeture de la fenêtre principale
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Récupérer le contrôleur et passer la réclamation
            StatusUpdateController controller = loader.getController();
            controller.setReclamation(reclamation);
            controller.setParentController(this);
            
            // Afficher la fenêtre
            stage.showAndWait();
            
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture de la fenêtre de mise à jour");
        }
    }

    private void handleDeleteAction(Reclamation reclamation) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            try {
            reclamationService.deleteReclamation(reclamation.getId());
            loadReclamationsAsync();
                showSuccess("Réclamation supprimée avec succès");
            } catch (Exception e) {
                showError("Erreur lors de la suppression de la réclamation");
            }
        }
    }

    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 