package Controlles;

import Entities.Reclamation;
import Entites.utilisateur;
import Services.ReclamationService;
import Services.utilisateurService;
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
    @FXML
    private Label clientIdLabel;
    @FXML
    private Label clientEmailLabel;
    @FXML
    private Label clientRegistrationDateLabel;
    
    @FXML
    private Label productIdLabel;
    @FXML
    private Label productNameLabel;
    @FXML
    private Label productPriceLabel;
    @FXML
    private Label productAvailabilityLabel;
    
    @FXML
    private Label reclamationIdLabel;
    @FXML
    private Label reclamationDateLabel;
    @FXML
    private Label reclamationTypeLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Button updateStatusButton;
    @FXML
    private TextArea messageArea;
    @FXML
    private Button backButton1;

    private final ReclamationService reclamationService = new ReclamationService();
    private final utilisateurService userService = new utilisateurService();
    private List<Reclamation> allReclamations = new ArrayList<>();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private Reclamation currentReclamation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        loadReclamationsAsync();
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Rafraîchissement des réclamations...");
        loadReclamationsAsync();
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReclamations(newValue);
        });

        allBox.setOnMouseClicked(event -> updateUI(allReclamations));
        pendingBox.setOnMouseClicked(event -> {
            List<Reclamation> filtered = allReclamations.stream()
                .filter(r -> r.getStatut_rec().equals("Pending"))
                .collect(Collectors.toList());
            updateUI(filtered);
        });
        inProgressBox.setOnMouseClicked(event -> {
            List<Reclamation> filtered = allReclamations.stream()
                .filter(r -> r.getStatut_rec().equals("In Progress"))
                .collect(Collectors.toList());
            updateUI(filtered);
        });
        resolvedBox.setOnMouseClicked(event -> {
            List<Reclamation> filtered = allReclamations.stream()
                .filter(r -> r.getStatut_rec().equals("Resolved"))
                .collect(Collectors.toList());
            updateUI(filtered);
        });
    }

    public void loadReclamationsAsync() {
        Task<List<Reclamation>> task = new Task<>() {
            @Override
            protected List<Reclamation> call() {
                return reclamationService.findAll();
            }
        };

        task.setOnSucceeded(event -> {
            allReclamations = task.getValue();
            updateUI(allReclamations);
        });

        task.setOnFailed(event -> {
            showError("Erreur lors du chargement des réclamations");
        });

        new Thread(task).start();
    }

    private void updateUI(List<Reclamation> reclamations) {
        Platform.runLater(() -> {
            reclamationsContainer.getChildren().clear();
            
            for (Reclamation reclamation : reclamations) {
                VBox card = createReclamationCard(reclamation);
                reclamationsContainer.getChildren().add(card);
            }

            updateCounters(reclamations);
        });
    }

    private VBox createReclamationCard(Reclamation reclamation) {
        // Création de la carte principale
        VBox card = new VBox(10);
        card.getStyleClass().add("reclamation-card");
        card.setPadding(new javafx.geometry.Insets(15));
        card.setMaxWidth(Double.MAX_VALUE);

        // En-tête de la carte
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Section ID et Type
        VBox titleBox = new VBox(5);
        Text id = new Text("Réclamation #" + reclamation.getId());
        id.getStyleClass().add("reclamation-id");
        Text type = new Text(reclamation.getType_rec());
        type.getStyleClass().add("reclamation-type");
        titleBox.getChildren().addAll(id, type);

        // Espaceur pour pousser les boutons à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Boutons d'action
        Button viewButton = new Button("Voir");
        viewButton.getStyleClass().add("view-button");
        viewButton.setOnAction(event -> showReclamationDetails(reclamation));

        Button processButton = new Button("Traiter");
        processButton.getStyleClass().add("action-button");
        processButton.setOnAction(event -> handleProcessAction(reclamation));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> handleDeleteAction(reclamation));

        HBox actions = new HBox(10);
        actions.getChildren().addAll(viewButton, processButton, deleteButton);

        // Assemblage de l'en-tête
        header.getChildren().addAll(titleBox, spacer, actions);

        // Section Informations
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // Informations Utilisateur
        VBox userInfo = new VBox(5);
        Text userLabel = new Text("Utilisateur:");
        userLabel.getStyleClass().add("info-label");
        Text userName = new Text(reclamation.getUser_name() != null ? reclamation.getUser_name() : "N/A");
        userName.getStyleClass().add("info-value");
        userInfo.getChildren().addAll(userLabel, userName);

        // Informations Produit
        VBox productInfo = new VBox(5);
        Text productLabel = new Text("Produit:");
        productLabel.getStyleClass().add("info-label");
        Text productName = new Text(reclamation.getProduct_name() != null ? reclamation.getProduct_name() : "N/A");
        productName.getStyleClass().add("info-value");
        productInfo.getChildren().addAll(productLabel, productName);

        infoBox.getChildren().addAll(userInfo, productInfo);

        // Description de la réclamation
        VBox descriptionBox = new VBox(5);
        Text descriptionLabel = new Text("Description:");
        descriptionLabel.getStyleClass().add("info-label");
        Text description = new Text(reclamation.getDescription_rec());
        description.getStyleClass().add("reclamation-description");
        descriptionBox.getChildren().addAll(descriptionLabel, description);

        // Message de la réclamation
        VBox messageBox = new VBox(5);
        Text messageLabel = new Text("Message:");
        messageLabel.getStyleClass().add("info-label");
        Text message = new Text(reclamation.getMessage_reclamation());
        message.getStyleClass().add("reclamation-message");
        messageBox.getChildren().addAll(messageLabel, message);

        // Pied de page avec date et statut
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        
        // Date de la réclamation
        Text date = new Text(DATE_FORMAT.format(reclamation.getDate_rec()));
        date.getStyleClass().add("reclamation-date");

        // Statut de la réclamation
        Label statusLabel = new Label(reclamation.getStatut_rec());
        switch (reclamation.getStatut_rec()) {
            case "Pending":
                statusLabel.getStyleClass().add("status-pending");
                break;
            case "In Progress":
                statusLabel.getStyleClass().add("status-in-progress");
                break;
            case "Resolved":
                statusLabel.getStyleClass().add("status-resolved");
                break;
            default:
                statusLabel.getStyleClass().add("status-unknown");
        }

        footer.getChildren().addAll(date, statusLabel);

        // Assemblage final de la carte
        card.getChildren().addAll(header, infoBox, descriptionBox, messageBox, footer);
        return card;
    }

    private void updateCounters(List<Reclamation> reclamations) {
        long total = reclamations.size();
        long pending = reclamations.stream().filter(r -> r.getStatut_rec().equals("Pending")).count();
        long inProgress = reclamations.stream().filter(r -> r.getStatut_rec().equals("In Progress")).count();
        long resolved = reclamations.stream().filter(r -> r.getStatut_rec().equals("Resolved")).count();

        allCount.setText(String.valueOf(total));
        pendingCount.setText(String.valueOf(pending));
        inProgressCount.setText(String.valueOf(inProgress));
        resolvedCount.setText(String.valueOf(resolved));
    }

    private void filterReclamations(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updateUI(allReclamations);
            return;
        }

        String searchLower = searchText.toLowerCase();
        List<Reclamation> filtered = allReclamations.stream()
                .filter(reclamation -> {
                    // Vérification du type de réclamation
                    String typeRec = reclamation.getType_rec();
                    boolean typeMatch = typeRec != null && typeRec.toLowerCase().contains(searchLower);
                    
                    // Vérification du message
                    String messageRec = reclamation.getMessage_reclamation();
                    boolean messageMatch = messageRec != null && messageRec.toLowerCase().contains(searchLower);
                    
                    // Vérification du nom d'utilisateur
                    String userName = reclamation.getUser_name();
                    boolean userMatch = userName != null && userName.toLowerCase().contains(searchLower);
                    
                    // Vérification du nom du produit
                    String productName = reclamation.getProduct_name();
                    boolean productMatch = productName != null && productName.toLowerCase().contains(searchLower);
                    
                    // Vérification de la description
                    String description = reclamation.getDescription_rec();
                    boolean descriptionMatch = description != null && description.toLowerCase().contains(searchLower);
                    
                    // Vérification du statut
                    String status = reclamation.getStatut_rec();
                    boolean statusMatch = status != null && status.toLowerCase().contains(searchLower);
                    
                    // Vérification de l'ID
                    String id = String.valueOf(reclamation.getId());
                    boolean idMatch = id.contains(searchLower);
                    
                    // Vérification de la date
                    String formattedDate = DATE_FORMAT.format(reclamation.getDate_rec());
                    boolean dateMatch = formattedDate.contains(searchLower);
                    
                    return typeMatch || messageMatch || userMatch || productMatch || 
                           descriptionMatch || statusMatch || idMatch || dateMatch;
                })
                .collect(Collectors.toList());

        updateUI(filtered);
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
        } catch (Exception e) {
            e.printStackTrace(); // Affiche l'erreur dans la console
            showError("Erreur lors de l'ouverture des détails : " + e.getMessage());
        }
    }

    private void handleProcessAction(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/StatusUpdate.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Mettre à jour le statut");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            StatusUpdateController controller = loader.getController();
            controller.setReclamation(reclamation);
            controller.setParentController(this);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace(); // Affiche l'erreur dans la console
            showError("Erreur lors de l'ouverture de la fenêtre de mise à jour : " + e.getMessage());
        }
    }

    private void handleDeleteAction(Reclamation reclamation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        ButtonType buttonTypeYes = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("Non", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(type -> {
            if (type == buttonTypeYes) {
                reclamationService.delete(reclamation);
                loadReclamationsAsync();
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateDisplay() {
        if (currentReclamation != null) {
            try {
                // Afficher l'ID de la reclamation
                reclamationIdLabel.setText(String.valueOf(currentReclamation.getId()));

                // Récupérer et afficher les informations de l'utilisateur
                utilisateur user = userService.findByID(currentReclamation.getUtilisateur_id());
                // ... existing code ...
            } catch (Exception e) {
                e.printStackTrace(); // Affiche l'erreur dans la console
                showError("Erreur lors de la mise à jour de l'affichage : " + e.getMessage());
            }
        }
    }
} 