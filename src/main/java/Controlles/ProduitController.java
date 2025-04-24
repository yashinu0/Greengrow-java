package Controlles;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
//import netscape.javascript.JSObject;
import Entites.Category;
import Entites.Produit;
import Services.ServiceProduit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import netscape.javascript.JSObject;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ProduitController implements Initializable {

    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, Integer> colId;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, String> colDescription;
    @FXML private TableColumn<Produit, Integer> colPrix;
    @FXML private TableColumn<Produit, Integer> colQuantite;
    @FXML private TableColumn<Produit, String> colDisponibilite;

    @FXML private TextField txtNom;
    @FXML private TextField txtDescription;
    @FXML private TextField txtPrix;
    @FXML private TextField txtQuantite;
    @FXML private TextField txtLocation;
    @FXML private TextField txtRating;
    @FXML private ComboBox<String> comboDisponibilite;
    @FXML private ComboBox<Category> comboCategories;
    @FXML private ImageView imagePreview;
    @FXML private Label lblImagePath;

    private ServiceProduit service = new ServiceProduit();
    private ObservableList<Produit> observableList = FXCollections.observableArrayList();
    private String imagePath = "";

    @FXML private WebView mapView;
    private String selectedLocation = "";

    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> availabilityBarChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        // Configure table columns
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colNom.setCellValueFactory(data -> data.getValue().nomProduitProperty());
        colDescription.setCellValueFactory(data -> data.getValue().descriptionProduitProperty());
        colPrix.setCellValueFactory(data -> data.getValue().prixProduitProperty().asObject());
        colQuantite.setCellValueFactory(data -> data.getValue().quantiteProperty().asObject());
        colDisponibilite.setCellValueFactory(data -> data.getValue().disponibilteProduitProperty());

        // Configure disponibilité combo box
        comboDisponibilite.getItems().addAll("Disponible", "En rupture", "Précommande");

        // Configure categories combo box
        try {
            List<Category> categories = service.getAllCategories();
            comboCategories.getItems().addAll(categories);

            comboCategories.setCellFactory(param -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNomCategories());
                    }
                }
            });

            comboCategories.setConverter(new StringConverter<Category>() {
                @Override
                public String toString(Category category) {
                    return category == null ? null : category.getNomCategories();
                }

                @Override
                public Category fromString(String string) {
                    return comboCategories.getItems().stream()
                            .filter(c -> c.getNomCategories().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement",
                    "Impossible de charger les catégories", Alert.AlertType.ERROR);
        }

        loadData();

        // Initialize map
        WebEngine webEngine = mapView.getEngine();
        JavaConnector connector = new JavaConnector();
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", connector);
            }
        });

        URL mapUrl = getClass().getResource("/map/map.html");
        webEngine.load(mapUrl.toExternalForm());

        // Initialize charts
        initializeCharts();
    }

    private void initializeCharts() {
        // Initialize pie chart
        categoryPieChart.setTitle("Distribution par Catégorie");
        categoryPieChart.setLegendVisible(true);
        categoryPieChart.setLabelsVisible(true);

        // Initialize bar chart
        availabilityBarChart.setTitle("Distribution par Disponibilité");
        availabilityBarChart.setLegendVisible(false);
    }

    private void updateCharts() {
        try {
            List<Produit> produits = service.afficher();
            
            // Update category pie chart
            Map<String, Long> categoryCount = produits.stream()
                .collect(Collectors.groupingBy(
                    p -> {
                        try {
                            return service.getCategoryName(p.getIdCategories());
                        } catch (SQLException e) {
                            return "Inconnue";
                        }
                    },
                    Collectors.counting()
                ));

            categoryPieChart.getData().clear();
            categoryCount.forEach((category, count) -> {
                PieChart.Data data = new PieChart.Data(category, count);
                categoryPieChart.getData().add(data);
            });

            // Update availability bar chart
            Map<String, Long> availabilityCount = produits.stream()
                .collect(Collectors.groupingBy(
                    Produit::getDisponibilteProduit,
                    Collectors.counting()
                ));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            availabilityCount.forEach((availability, count) -> {
                series.getData().add(new XYChart.Data<>(availability, count));
            });

            availabilityBarChart.getData().clear();
            availabilityBarChart.getData().add(series);

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement",
                    "Impossible de charger les statistiques", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        // Set initial directory to your desired path
        File initialDir = new File("C:/Users/USER/integration/smart farming/src/main/resources/images");
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Create an images directory if it doesn't exist
                Path imagesDir = Paths.get("src/main/resources/images");
                if (!Files.exists(imagesDir)) {
                    Files.createDirectories(imagesDir);
                }

                // Copy the selected file to our images directory
                Path destination = imagesDir.resolve(selectedFile.getName());
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Update the UI
                imagePath = destination.toString();
                lblImagePath.setText(selectedFile.getName());
                imagePreview.setImage(new Image(destination.toUri().toString()));
            } catch (IOException e) {
                showAlert("Erreur", "Erreur de fichier",
                        "Impossible de copier l'image: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void loadData() {
        observableList.clear();
        try {
            List<Produit> produits = service.afficher();
            observableList.addAll(produits);
            tableProduits.setItems(observableList);
            updateCharts(); // Update charts when data is loaded
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement",
                    "Impossible de charger les produits", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void ajouterProduit() {
        try {
            // Validate required fields
            if (txtNom.getText().isEmpty() || txtDescription.getText().isEmpty() ||
                    txtPrix.getText().isEmpty() || txtQuantite.getText().isEmpty() ||
                    comboDisponibilite.getValue() == null || comboCategories.getValue() == null) {
                showAlert("Erreur", "Champs manquants",
                        "Veuillez remplir tous les champs obligatoires", Alert.AlertType.ERROR);
                return;
            }

            Produit p = new Produit();
            p.setNomProduit(txtNom.getText());
            p.setDescriptionProduit(txtDescription.getText());

            try {
                p.setPrixProduit(Integer.parseInt(txtPrix.getText()));
                p.setQuantite(Integer.parseInt(txtQuantite.getText()));

            } catch (NumberFormatException e) {
                showAlert("Erreur", "Valeur invalide",
                        "Prix, quantité et rating doivent être des nombres", Alert.AlertType.ERROR);
                return;
            }

            p.setDisponibilteProduit(comboDisponibilite.getValue());
            p.setIdCategories(comboCategories.getValue().getId());
            p.setImageProduit(imagePath);
            p.setLocation(selectedLocation);

            service.ajouter(p);
            loadData();
            clearFields();
            showAlert("Succès", "Produit ajouté",
                    "Le produit a été ajouté avec succès", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de base de données",
                    e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void modifierProduit() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Aucune sélection",
                    "Veuillez sélectionner un produit à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Validate required fields
            if (txtNom.getText().isEmpty() || txtDescription.getText().isEmpty() ||
                    txtPrix.getText().isEmpty() || txtQuantite.getText().isEmpty() ||
                    comboDisponibilite.getValue() == null || comboCategories.getValue() == null) {
                showAlert("Erreur", "Champs manquants",
                        "Veuillez remplir tous les champs obligatoires", Alert.AlertType.ERROR);
                return;
            }

            selected.setNomProduit(txtNom.getText());
            selected.setDescriptionProduit(txtDescription.getText());

            try {
                selected.setPrixProduit(Integer.parseInt(txtPrix.getText()));
                selected.setQuantite(Integer.parseInt(txtQuantite.getText()));
                if (!txtRating.getText().isEmpty()) {
                    selected.setRating(Double.parseDouble(txtRating.getText()));
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Valeur invalide",
                        "Prix, quantité et rating doivent être des nombres", Alert.AlertType.ERROR);
                return;
            }

            selected.setDisponibilteProduit(comboDisponibilite.getValue());
            selected.setIdCategories(comboCategories.getValue().getId());
            selected.setLocation(txtLocation.getText());

            // Only update image if a new one was selected
            if (!imagePath.isEmpty()) {
                selected.setImageProduit(imagePath);
            }

            service.modifier(selected);
            loadData();
            clearFields();
            showAlert("Succès", "Produit modifié",
                    "Le produit a été modifié avec succès", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de base de données",
                    e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void supprimerProduit() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Aucune sélection",
                    "Veuillez sélectionner un produit à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le produit");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(selected.getId());
                loadData();
                clearFields();
                showAlert("Succès", "Produit supprimé",
                        "Le produit a été supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur de suppression",
                        e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void tableClicked() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtNom.setText(selected.getNomProduit());
            txtDescription.setText(selected.getDescriptionProduit());
            txtPrix.setText(String.valueOf(selected.getPrixProduit()));
            txtQuantite.setText(String.valueOf(selected.getQuantite()));
            txtLocation.setText(selected.getLocation());
            txtRating.setText(String.valueOf(selected.getRating()));
            comboDisponibilite.setValue(selected.getDisponibilteProduit());

            // Find and select the corresponding category
            comboCategories.getItems().stream()
                    .filter(c -> c.getId() == selected.getIdCategories())
                    .findFirst()
                    .ifPresent(comboCategories.getSelectionModel()::select);

            // Load image if exists
            if (selected.getImageProduit() != null && !selected.getImageProduit().isEmpty()) {
                imagePath = selected.getImageProduit();
                lblImagePath.setText(new File(imagePath).getName());
                imagePreview.setImage(new Image(new File(imagePath).toURI().toString()));
            } else {
                imagePath = "";
                lblImagePath.setText("No image selected");
                imagePreview.setImage(new Image(getClass().getResource("/assets/default-product.png").toString()));
            }
        }
    }

    private void clearFields() {
        txtNom.clear();
        txtDescription.clear();
        txtPrix.clear();
        txtQuantite.clear();
        txtLocation.clear();
        txtRating.clear();
        comboDisponibilite.getSelectionModel().clearSelection();
        comboCategories.getSelectionModel().clearSelection();
        imagePath = "";
        lblImagePath.setText("No image selected");
        imagePreview.setImage(new Image(getClass().getResource("/assets/default-product.png").toString()));
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void selectLocation() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Location");
        dialog.setHeaderText("Enter coordinates (latitude,longitude)");
        dialog.setContentText("Format: 36.8065,10.1815");

        dialog.showAndWait().ifPresent(result -> {
            txtLocation.setText(result);
        });
    }

    @FXML
    private void openMap() {
        // The map is already loaded in the WebView, just make sure it's visible
        mapView.setVisible(true);
    }

    @FXML
    private void validateLocation() {
        if (selectedLocation.isEmpty()) {
            showAlert("Avertissement", "Localisation manquante",
                    "Veuillez sélectionner une localisation sur la carte", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Succès", "Localisation validée",
                "La localisation a été validée avec succès: " + selectedLocation, Alert.AlertType.INFORMATION);
    }

    public class JavaConnector {
        public void setLocation(String location) {
            Platform.runLater(() -> {
                selectedLocation = location;
                txtLocation.setText(location);
            });
        }
    }

}