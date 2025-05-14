package Controlles;

import Entities.Livreur;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.UUID;

public class LivreurViewController {
    @FXML
    private TextField nomLivreurTextField;
    @FXML
    private TextField prenomLivreurTextField;
    @FXML
    private TextField numeroLivreurTextField;
    @FXML
    private TextField addresseLivreurTextField;
    @FXML
    private ImageView photoLivreurImageView;

    private LivreurController livreurController;
    private Livreur livreur;
    private String tempPhotoPath;
    private Stage primaryStage;
    private static final String UPLOAD_DIR = "uploads/photos";

    public void initialize() {
        livreurController = new LivreurController();
        createUploadDirectory();
    }

    public void setLivreur(Livreur livreur) {
        this.livreur = livreur;
        if (livreur != null) {
            nomLivreurTextField.setText(livreur.getNomLivreur());
            prenomLivreurTextField.setText(livreur.getPrenomLivreur());
            numeroLivreurTextField.setText(livreur.getNumeroLivreur());
            addresseLivreurTextField.setText(livreur.getAddresseLivreur());
            if (livreur.getPhotoLivreur() != null) {
                loadPhoto(livreur.getPhotoLivreur());
            }
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleChoisirPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                String fileName = savePhoto(file.toPath());
                loadPhoto(fileName);
                tempPhotoPath = fileName;
            } catch (IOException e) {
                showError("Erreur lors du chargement de la photo", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEnregistrer() {
        if (!validateInputs()) {
            return;
        }

        if (livreur == null) {
            livreur = new Livreur();
        }

        livreur.setNomLivreur(nomLivreurTextField.getText());
        livreur.setPrenomLivreur(prenomLivreurTextField.getText());
        livreur.setNumeroLivreur(numeroLivreurTextField.getText());
        livreur.setAddresseLivreur(addresseLivreurTextField.getText());

        if (tempPhotoPath != null) {
            livreur.setPhotoLivreur(tempPhotoPath);
        }

        try {
            if (livreur.getId() == 0) {
                livreurController.ajouterLivreur(livreur);
            } else {
                livreurController.updateLivreur(livreur);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Livreur enregistré avec succès !");
            alert.showAndWait();
            clearForm();
        } catch (SQLException e) {
            showError("Erreur", "Erreur lors de l'enregistrement du livreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        clearForm();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomLivreurTextField.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (nomLivreurTextField.getText().trim().isEmpty()) {
            errors.append("Le nom est obligatoire\n");
        }
        if (prenomLivreurTextField.getText().trim().isEmpty()) {
            errors.append("Le prénom est obligatoire\n");
        }
        if (!numeroLivreurTextField.getText().matches("^[0-9]{8,15}$")) {
            errors.append("Le numéro de téléphone n'est pas valide\n");
        }
        if (addresseLivreurTextField.getText().trim().isEmpty()) {
            errors.append("L'adresse est obligatoire\n");
        }

        if (errors.length() > 0) {
            showError("Erreur de validation", errors.toString());
            return false;
        }

        return true;
    }

    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            showError("Erreur", "Impossible de créer le dossier des photos");
        }
    }

    private String savePhoto(Path sourcePath) throws IOException {
        String fileName = UUID.randomUUID().toString() + getFileExtension(sourcePath.toString());
        Path targetPath = Paths.get(UPLOAD_DIR, fileName);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private void loadPhoto(String fileName) {
        try {
            Path photoPath = Paths.get(UPLOAD_DIR, fileName);
            if (Files.exists(photoPath)) {
                Image image = new Image(photoPath.toUri().toString());
                photoLivreurImageView.setImage(image);
            }
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger la photo");
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        nomLivreurTextField.clear();
        prenomLivreurTextField.clear();
        numeroLivreurTextField.clear();
        addresseLivreurTextField.clear();
        tempPhotoPath = null;
        livreur = null;
    }
}
