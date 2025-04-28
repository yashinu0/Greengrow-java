package Controlles;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import Entites.Category;
import Services.ServiceCategorie;

import java.sql.SQLException;
import java.util.List;

public class CategoryController {

    @FXML private TableView<Category> tableCategories;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colNom;
    @FXML private TableColumn<Category, String> colDescription;

    @FXML private TextField txtNom;
    @FXML private TextField txtDescription;

    private ServiceCategorie service = new ServiceCategorie();
    private ObservableList<Category> observableList = FXCollections.observableArrayList();

    public CategoryController() throws SQLException {
    }

    public void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNomCategories()));
        colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescriptionCategories()));
        loadData();
    }

    private void loadData() {
        observableList.clear();
        try {
            System.out.println("Attempting to load categories...");
            List<Category> categories = service.afficher();
            System.out.println("Successfully loaded " + categories.size() + " categories");
            observableList.addAll(categories);
            tableCategories.setItems(observableList);
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
            // Show error alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Error loading categories");
            alert.setContentText("Please check your database connection and try again.\nError: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void ajouterCategorie() {
        try {
            Category c = new Category();
            c.setNomCategories(txtNom.getText());
            c.setDescriptionCategories(txtDescription.getText());
            service.ajouter(c);
            loadData();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierCategorie() {
        Category selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setNomCategories(txtNom.getText());
            selected.setDescriptionCategories(txtDescription.getText());
            try {
                service.modifier(selected);
                loadData();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void supprimerCategorie() {
        Category selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                service.supprimer(selected.getId());
                loadData();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void tableClicked() {
        Category selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtNom.setText(selected.getNomCategories());
            txtDescription.setText(selected.getDescriptionCategories());
        }
    }

    private void clearFields() {
        txtNom.clear();
        txtDescription.clear();
    }
}
