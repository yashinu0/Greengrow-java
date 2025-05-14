package Controlles;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.chart.PieChart;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class RenduListController {
    @FXML private ListView<HBox> renduListView;
    @FXML private Label plantTypeSummaryLabel;
    @FXML private PieChart plantTypePieChart;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private ComboBox<String> orderComboBox;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageInfoLabel;

    private final RenduService renduService = new RenduService();
    private final ObservableList<Rendu> rendus = FXCollections.observableArrayList();
    private final int PAGE_SIZE = 4;
    private int currentPage = 1;
    private int totalPages = 1;
    private ObservableList<Rendu> filteredSortedRendus = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupSortOptions();
        setupListeners();
        loadRendus();
    }

    private void setupSortOptions() {
        sortComboBox.getItems().addAll("ID", "Type", "Date");
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

    private void loadRendus() {
        try {
            rendus.setAll(renduService.getAllRendus());
            applyFiltersAndPagination();
            // Count plant types
            HashMap<String, Integer> typeCounts = new HashMap<>();
            for (Rendu rendu : rendus) {
                String type = rendu.getType_rendu();
                typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
            }
            // Set summary label
            StringBuilder summary = new StringBuilder();
            for (String type : typeCounts.keySet()) {
                if (summary.length() > 0) summary.append(" | ");
                summary.append(type).append(": ").append(typeCounts.get(type));
            }
            plantTypeSummaryLabel.setText(summary.toString());
            // Populate pie chart
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (String type : typeCounts.keySet()) {
                pieChartData.add(new PieChart.Data(type, typeCounts.get(type)));
            }
            plantTypePieChart.setData(pieChartData);
            plantTypePieChart.setLegendVisible(false);
        } catch (SQLException e) {
            showAlert("Error loading Rendu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void applyFiltersAndPagination() {
        // Filter
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        filteredSortedRendus.setAll(rendus.filtered(r ->
            r.getMessage_rendu().toLowerCase().contains(search) ||
            r.getType_rendu().toLowerCase().contains(search) ||
            String.valueOf(r.getId_rendu()).contains(search) ||
            r.getDate_envoi_rendu().toString().contains(search)
        ));
        // Sort
        String sortField = sortComboBox.getValue();
        boolean ascending = orderComboBox.getValue().equals("Ascending");
        filteredSortedRendus.sort((a, b) -> {
            int cmp = 0;
            if ("ID".equals(sortField)) {
                cmp = Integer.compare(a.getId_rendu(), b.getId_rendu());
            } else if ("Type".equals(sortField)) {
                cmp = a.getType_rendu().compareToIgnoreCase(b.getType_rendu());
            } else if ("Date".equals(sortField)) {
                cmp = a.getDate_envoi_rendu().compareTo(b.getDate_envoi_rendu());
            }
            return ascending ? cmp : -cmp;
        });
        // Pagination
        currentPage = 1;
        updateListView();
    }

    private void updateListView() {
        renduListView.getItems().clear();
        int totalItems = filteredSortedRendus.size();
        totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        int fromIndex = (currentPage - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalItems);
        for (int i = fromIndex; i < toIndex; i++) {
            renduListView.getItems().add(createRenduItem(filteredSortedRendus.get(i)));
        }
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        prevPageBtn.setDisable(currentPage == 1);
        nextPageBtn.setDisable(currentPage == totalPages);
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
        ((Stage) renduListView.getScene().getWindow()).close();
    }

    @FXML
    private void handleAddRendu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BackRendu.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add Rendu");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            // Refresh the list after adding
            loadRendus();
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