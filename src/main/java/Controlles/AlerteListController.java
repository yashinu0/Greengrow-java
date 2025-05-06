package Controlles;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AlerteListController {
    @FXML private ListView<HBox> alerteListView;
    @FXML private Label alerteSummaryLabel;
    @FXML private PieChart alertePieChart;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private ComboBox<String> orderComboBox;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageInfoLabel;

    private final AlerteService alerteService = new AlerteService();
    private final ObservableList<Alerte> alertes = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final int PAGE_SIZE = 4;
    private int currentPage = 1;
    private int totalPages = 1;
    private ObservableList<Alerte> filteredSortedAlertes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupSortOptions();
        setupListeners();
        loadAlertes();
    }

    private void setupSortOptions() {
        sortComboBox.getItems().addAll("ID", "Urgency", "Date");
        sortComboBox.getSelectionModel().selectFirst();
        orderComboBox.getItems().addAll("Ascending", "Descending");
        orderComboBox.getSelectionModel().selectFirst();
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndPagination());
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndPagination());
        orderComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndPagination());
        prevPageBtn.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateListView();
            }
        });
        nextPageBtn.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateListView();
            }
        });
    }

    private void loadAlertes() {
        try {
            alertes.setAll(alerteService.getAllAlertes());
            applyFiltersAndPagination();
            int safeCount = 0;
            int criticalCount = 0;
            for (Alerte alerte : alertes) {
                String urgency = alerte.getNiveau_urgence_alerte().toLowerCase();
                if (urgency.contains("critical")) {
                    criticalCount++;
                } else if (urgency.contains("safe")) {
                    safeCount++;
                }
            }
            alerteSummaryLabel.setText("Safe: " + safeCount + " | Critical: " + criticalCount);
            javafx.collections.ObservableList<PieChart.Data> pieChartData = javafx.collections.FXCollections.observableArrayList();
            if (safeCount > 0) pieChartData.add(new PieChart.Data("Safe", safeCount));
            if (criticalCount > 0) pieChartData.add(new PieChart.Data("Critical", criticalCount));
            alertePieChart.setData(pieChartData);
            alertePieChart.setLegendVisible(false);
        } catch (SQLException e) {
            showAlert("Error loading alerts: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void applyFiltersAndPagination() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        filteredSortedAlertes.setAll(alertes.filtered(a ->
            String.valueOf(a.getId_alerte()).contains(search) ||
            a.getNiveau_urgence_alerte().toLowerCase().contains(search) ||
            (a.getRendu() != null && a.getRendu().getMessage_rendu().toLowerCase().contains(search)) ||
            a.getTemps_limite_alerte().toString().contains(search)
        ));
        String sortField = sortComboBox.getValue();
        boolean ascending = orderComboBox.getValue().equals("Ascending");
        filteredSortedAlertes.sort((a, b) -> {
            int cmp = 0;
            if ("ID".equals(sortField)) {
                cmp = Integer.compare(a.getId_alerte(), b.getId_alerte());
            } else if ("Urgency".equals(sortField)) {
                cmp = a.getNiveau_urgence_alerte().compareToIgnoreCase(b.getNiveau_urgence_alerte());
            } else if ("Date".equals(sortField)) {
                cmp = a.getTemps_limite_alerte().compareTo(b.getTemps_limite_alerte());
            }
            return ascending ? cmp : -cmp;
        });
        currentPage = 1;
        updateListView();
    }

    private void updateListView() {
        alerteListView.getItems().clear();
        int totalItems = filteredSortedAlertes.size();
        totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        int fromIndex = (currentPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);
        for (int i = fromIndex; i < toIndex; i++) {
            alerteListView.getItems().add(createAlerteItem(filteredSortedAlertes.get(i)));
        }
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        prevPageBtn.setDisable(currentPage == 1);
        nextPageBtn.setDisable(currentPage == totalPages);
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
        ((Stage) alerteListView.getScene().getWindow()).close();
    }

    @FXML
    private void handleAddAlerte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BackAlerte.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add Alerte");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            // Refresh the list after adding
            loadAlertes();
        } catch (IOException e) {
            showAlert("Error loading add form: " + e.getMessage(), Alert.AlertType.ERROR);
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