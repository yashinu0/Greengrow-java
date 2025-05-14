package view;

import Controlles.LivreurController;
import Controlles.LivreurViewController;
import Entities.Livreur;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class LivreurView {
    private LivreurController controller;
    private TableView<Livreur> tableLivreurs;
    private TextField nomLivreurField;
    private TextField prenomLivreurField;
    private TextField numeroLivreurField;
    private TextField addresseLivreurField;
    private TextField photoLivreurField;
    private Stage stage;

    public LivreurView(Stage stage) {
        this.stage = stage;
        this.controller = new LivreurController();
        createView();
    }

    private void createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStylesheets().add(getClass().getResource("/styles/nature-theme.css").toExternalForm());

        // Titre
        Label titre = new Label("Gestion des Livreurs");
        titre.getStyleClass().add("window-title");
        root.getChildren().add(titre);

        // Formulaire
        VBox formContainer = new VBox(15);
        formContainer.getStyleClass().add("grid-pane");
        
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(20));

        Label nomLivreurLabel = new Label("Nom livreur:");
        nomLivreurField = new TextField();
        
        Label prenomLivreurLabel = new Label("Prénom livreur:");
        prenomLivreurField = new TextField();
        
        Label numeroLivreurLabel = new Label("Numéro livreur:");
        numeroLivreurField = new TextField();
        
        Label addresseLivreurLabel = new Label("Addresse livreur:");
        addresseLivreurField = new TextField();
        
        Label photoLivreurLabel = new Label("Photo livreur:");
        photoLivreurField = new TextField();

        form.add(nomLivreurLabel, 0, 0);
        form.add(nomLivreurField, 1, 0);
        form.add(prenomLivreurLabel, 0, 1);
        form.add(prenomLivreurField, 1, 1);
        form.add(numeroLivreurLabel, 0, 2);
        form.add(numeroLivreurField, 1, 2);
        form.add(addresseLivreurLabel, 0, 3);
        form.add(addresseLivreurField, 1, 3);
        form.add(photoLivreurLabel, 0, 4);
        form.add(photoLivreurField, 1, 4);

        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        Button ajouterBtn = new Button("Ajouter");
        Button modifierBtn = new Button("Modifier");
        Button modifierAvecPhotoBtn = new Button("Modifier avec photo");
        Button supprimerBtn = new Button("Supprimer");
        Button effacerBtn = new Button("Effacer");
        
        buttonBox.getChildren().addAll(ajouterBtn, modifierBtn, modifierAvecPhotoBtn, supprimerBtn, effacerBtn);
        
        formContainer.getChildren().addAll(form, buttonBox);
        root.getChildren().add(formContainer);

        // Table
        tableLivreurs = new TableView<>();
        tableLivreurs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Livreur, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idCol.setPrefWidth(50);
        
        TableColumn<Livreur, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(cellData -> cellData.getValue().nomLivreurProperty());
        
        TableColumn<Livreur, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(cellData -> cellData.getValue().prenomLivreurProperty());
        
        TableColumn<Livreur, String> numeroCol = new TableColumn<>("Numéro");
        numeroCol.setCellValueFactory(cellData -> cellData.getValue().numeroLivreurProperty());
        
        TableColumn<Livreur, String> addresseCol = new TableColumn<>("Adresse");
        addresseCol.setCellValueFactory(cellData -> cellData.getValue().addresseLivreurProperty());
        
        TableColumn<Livreur, String> photoCol = new TableColumn<>("Photo");
        photoCol.setCellValueFactory(cellData -> cellData.getValue().photoLivreurProperty());

        tableLivreurs.getColumns().addAll(idCol, nomCol, prenomCol, numeroCol, addresseCol, photoCol);
        tableLivreurs.setPrefHeight(400);
        
        VBox tableContainer = new VBox(tableLivreurs);
        tableContainer.getStyleClass().add("grid-pane");
        tableContainer.setPadding(new Insets(20, 0, 0, 0));
        root.getChildren().add(tableContainer);

        // Events
        ajouterBtn.setOnAction(e -> ajouterLivreur());
        modifierBtn.setOnAction(e -> modifierLivreur());
        modifierAvecPhotoBtn.setOnAction(e -> modifierLivreurAvecPhoto());
        supprimerBtn.setOnAction(e -> supprimerLivreur());
        effacerBtn.setOnAction(e -> effacerFormulaire());
        
        tableLivreurs.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nomLivreurField.setText(newSelection.getNomLivreur());
                prenomLivreurField.setText(newSelection.getPrenomLivreur());
                numeroLivreurField.setText(newSelection.getNumeroLivreur());
                addresseLivreurField.setText(newSelection.getAddresseLivreur());
                photoLivreurField.setText(newSelection.getPhotoLivreur());
            }
        });

        Scene scene = new Scene(root);
        stage.setTitle("Gestion des Livreurs");
        stage.setScene(scene);
        stage.show();

        // Charger les données
        rafraichirTable();
    }

    private void ajouterLivreur() {
        if (!validerFormulaire()) return;

        Livreur livreur = new Livreur();
        livreur.setNomLivreur(nomLivreurField.getText());
        livreur.setPrenomLivreur(prenomLivreurField.getText());
        livreur.setNumeroLivreur(numeroLivreurField.getText());
        livreur.setAddresseLivreur(addresseLivreurField.getText());
        livreur.setPhotoLivreur(photoLivreurField.getText());
                
        try {
            controller.ajouterLivreur(livreur);
            effacerFormulaire();
            rafraichirTable();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Livreur ajouté avec succès!");
            alert.showAndWait();
        } catch (SQLException e) {
            afficherErreur("Erreur lors de l'ajout du livreur: " + e.getMessage());
        }
    }

    private void modifierLivreur() {
        Livreur selected = tableLivreurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Veuillez sélectionner un livreur à modifier");
            return;
        }

        if (!validerFormulaire()) return;

        selected.setNomLivreur(nomLivreurField.getText());
        selected.setPrenomLivreur(prenomLivreurField.getText());
        selected.setNumeroLivreur(numeroLivreurField.getText());
        selected.setAddresseLivreur(addresseLivreurField.getText());
        selected.setPhotoLivreur(photoLivreurField.getText());
                
        try {
            controller.updateLivreur(selected);
            effacerFormulaire();
            rafraichirTable();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Livreur modifié avec succès!");
            alert.showAndWait();
        } catch (SQLException e) {
            afficherErreur("Erreur lors de la modification du livreur: " + e.getMessage());
        }
    }

    private void modifierLivreurAvecPhoto() {
        Livreur selected = tableLivreurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Veuillez sélectionner un livreur à modifier");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gestioncommandes/view/livreur-view.fxml"));
            Scene scene = new Scene(loader.load());
            
            Stage photoStage = new Stage();
            photoStage.setTitle("Modifier le livreur avec photo");
            
            LivreurViewController controller = loader.getController();
            controller.setPrimaryStage(photoStage);
            controller.setLivreur(selected);
            
            photoStage.setScene(scene);
            photoStage.showAndWait();
            
            // Rafraîchir la table après la modification
            rafraichirTable();
        } catch (Exception e) {
            afficherErreur("Erreur lors de l'ouverture de la vue de modification avec photo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void supprimerLivreur() {
        Livreur selected = tableLivreurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Veuillez sélectionner un livreur à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce livreur ?");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                controller.deleteLivreur(selected.getId());
                effacerFormulaire();
                rafraichirTable();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Livreur supprimé avec succès!");
                alert.showAndWait();
            } catch (SQLException e) {
                afficherErreur("Erreur lors de la suppression du livreur: " + e.getMessage());
            }
        }
    }

    private void effacerFormulaire() {
        nomLivreurField.clear();
        prenomLivreurField.clear();
        numeroLivreurField.clear();
        addresseLivreurField.clear();
        photoLivreurField.clear();
        tableLivreurs.getSelectionModel().clearSelection();
    }

    private void rafraichirTable() {
        try {
            tableLivreurs.getItems().clear();
            tableLivreurs.getItems().addAll(controller.getAllLivreurs());
        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des livreurs: " + e.getMessage());
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (nomLivreurField.getText().trim().isEmpty()) {
            erreurs.append("Le nom du livreur est obligatoire\n");
        }

        if (prenomLivreurField.getText().trim().isEmpty()) {
            erreurs.append("Le prénom du livreur est obligatoire\n");
        }

        String numero = numeroLivreurField.getText().trim();
        if (!numero.matches("\\d{8}")) {
            erreurs.append("Le numéro du livreur doit contenir exactement 8 chiffres\n");
        }

        String addresse = addresseLivreurField.getText().trim();
        if (addresse.isEmpty()) {
            erreurs.append("L'adresse du livreur est obligatoire\n");
        }

        if (erreurs.length() > 0) {
            afficherErreur(erreurs.toString());
            return false;
        }

        return true;
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
