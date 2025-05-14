package Controlles;

import Entities.Commande;
import Utils.MyDB;
import util.PDFGenerator;
import Controlles.RouletteController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class CommandeController implements Initializable {

    @FXML private ComboBox<Integer> livreurIdComboBox;
    @FXML private TextField prixTotalField;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private ComboBox<String> modePaiementComboBox;
    @FXML private TableView<Commande> commandeTable;
    @FXML private TableColumn<Commande, Integer> idColumn;
    @FXML private TableColumn<Commande, Integer> livreurIdColumn;
    @FXML private TableColumn<Commande, String> statutColumn;
    @FXML private TableColumn<Commande, LocalDateTime> dateColumn;
    @FXML private TableColumn<Commande, Double> prixTotalColumn;
    @FXML private TableColumn<Commande, String> modePaiementColumn;
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> orderComboBox;

    private ObservableList<Commande> commandeList = FXCollections.observableArrayList();
    private double montantOriginal = 0.0;

    private void chargerLivreurs() {
        ObservableList<Integer> livreurIds = FXCollections.observableArrayList();
        String sql = "SELECT id FROM livreur ORDER BY id";
        
        try (Statement stmt = MyDB.getInstance().getCon().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                livreurIds.add(rs.getInt("id"));
            }
            
            livreurIdComboBox.setItems(livreurIds);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des livreurs: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Charger les livreurs disponibles
        chargerLivreurs();

        // Initialiser les colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idCommande"));
        livreurIdColumn.setCellValueFactory(new PropertyValueFactory<>("livreurCommandeId"));
        statutColumn.setCellValueFactory(cellData -> {
            Commande commande = cellData.getValue();
            String statut = commande.getStatutCommande();
            return new SimpleStringProperty(statut);
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        prixTotalColumn.setCellValueFactory(new PropertyValueFactory<>("prixTotalCommande"));
        modePaiementColumn.setCellValueFactory(new PropertyValueFactory<>("modePaiementCommande"));

        // Initialiser le ComboBox des statuts
        statutComboBox.setItems(FXCollections.observableArrayList(
            "en_preparation", "livree", "annulee"
        ));

        modePaiementComboBox.setItems(FXCollections.observableArrayList(
            "carte", "cheque"
        ));

        // Initialiser les ComboBox de filtrage
        filterTypeComboBox.setItems(FXCollections.observableArrayList(
            "Tous", "Statut", "Montant"
        ));
        filterTypeComboBox.setValue("Tous");

        orderComboBox.setItems(FXCollections.observableArrayList(
            "Croissant", "Décroissant"
        ));
        orderComboBox.setValue("Croissant");

        // Ajouter les listeners pour le filtrage
        filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filtrerCommandes());
        orderComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filtrerCommandes());

        // Lier la table avec la liste observable
        commandeTable.setItems(commandeList);

        // Charger les données
        chargerCommandes();

        // Ajouter un listener pour la sélection dans la table
        commandeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                livreurIdComboBox.setValue(newSelection.getLivreurCommandeId());
                prixTotalField.setText(String.valueOf(newSelection.getPrixTotalCommande()));
                statutComboBox.setValue(newSelection.getStatutCommande());
                modePaiementComboBox.setValue(newSelection.getModePaiementCommande());
            }
        });
    }

    @FXML
    private void handleAjouter() {
        try {
            // Vérification du livreur sélectionné
            Integer livreurId = livreurIdComboBox.getValue();
            if (livreurId == null) {
                showAlert("Erreur", "Veuillez sélectionner un livreur");
                return;
            }

            String sql = "INSERT INTO commande (livreur_commande_id, statue_commande, date_commande, prixtotal_commande, modepaiement_commande) VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = MyDB.getInstance().getCon().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, livreurId);
                pstmt.setString(2, statutComboBox.getValue());
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setDouble(4, Double.parseDouble(prixTotalField.getText()));
                pstmt.setString(5, modePaiementComboBox.getValue());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            Commande commande = new Commande();
                            commande.setIdCommande(rs.getInt(1));
                            commande.setLivreurCommandeId(livreurId);
                            commande.setDateCommande(LocalDateTime.now());
                            commande.setStatutCommande(statutComboBox.getValue());
                            commande.setPrixTotalCommande(Double.parseDouble(prixTotalField.getText()));
                            commande.setModePaiementCommande(modePaiementComboBox.getValue());
                            commandeList.add(commande);
                            clearFields();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout de la commande: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le montant doit être un nombre valide");
        }
    }

    @FXML
    private void handleModifier() {
        Commande commande = commandeTable.getSelectionModel().getSelectedItem();
        if (commande == null) {
            showAlert("Erreur", "Veuillez sélectionner une commande à modifier");
            return;
        }

        // Vérification du livreur sélectionné
        Integer livreurId = livreurIdComboBox.getValue();
        if (livreurId == null) {
            showAlert("Erreur", "Veuillez sélectionner un livreur");
            return;
        }

        try {
            String sql = "UPDATE commande SET livreur_commande_id = ?, statue_commande = ?, prixtotal_commande = ?, modepaiement_commande = ? WHERE id_commande = ?";
            try (PreparedStatement pstmt = MyDB.getInstance().getCon().prepareStatement(sql)) {
                pstmt.setInt(1, livreurId);
                pstmt.setString(2, statutComboBox.getValue());
                pstmt.setDouble(3, Double.parseDouble(prixTotalField.getText()));
                pstmt.setString(4, modePaiementComboBox.getValue());
                pstmt.setInt(5, commande.getIdCommande());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    commande.setLivreurCommandeId(livreurId);
                    commande.setStatutCommande(statutComboBox.getValue());
                    commande.setPrixTotalCommande(Double.parseDouble(prixTotalField.getText()));
                    commande.setModePaiementCommande(modePaiementComboBox.getValue());
                    commandeTable.refresh();
                    clearFields();
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification de la commande: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le montant doit être un nombre valide");
        }
    }

    @FXML
    private void handleSupprimer() {
        Commande commande = commandeTable.getSelectionModel().getSelectedItem();
        if (commande == null) {
            showAlert("Erreur", "Veuillez sélectionner une commande à supprimer");
            return;
        }

        try {
            String sql = "DELETE FROM commande WHERE id_commande = ?";
            try (PreparedStatement pstmt = MyDB.getInstance().getCon().prepareStatement(sql)) {
                pstmt.setInt(1, commande.getIdCommande());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    commandeList.remove(commande);
                    clearFields();
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression de la commande: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenererFacture() {
        Commande selectedCommande = commandeTable.getSelectionModel().getSelectedItem();
        if (selectedCommande == null) {
            showAlert("Erreur", "Veuillez sélectionner une commande pour générer la facture");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la facture");
        fileChooser.setInitialFileName("facture_" + selectedCommande.getIdCommande() + ".pdf");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(commandeTable.getScene().getWindow());
        if (file != null) {
            try {
                PDFGenerator.generateInvoice(selectedCommande, file.getAbsolutePath());
                
                // Générer le QR code après avoir créé la facture
                String qrCodePath = file.getParent() + File.separator + "qrcode_" + selectedCommande.getIdCommande() + ".png";
                generateQRCode(file.getAbsolutePath(), qrCodePath);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("La facture et le QR code ont été générés avec succès !");
                alert.showAndWait();
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la génération de la facture: " + e.getMessage());
            }
        }
    }

    private void generateQRCode(String factureUrl, String qrCodePath) throws WriterException, IOException {
        int width = 300;
        int height = 300;
        String fileUrl = "file://" + factureUrl.replace("\\", "/");
        
        BitMatrix matrix = new MultiFormatWriter().encode(
            fileUrl,
            BarcodeFormat.QR_CODE,
            width,
            height
        );

        MatrixToImageWriter.writeToPath(
            matrix,
            "PNG",
            new File(qrCodePath).toPath()
        );
    }

    @FXML
    private void handleAfficherStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/statistiques-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Statistiques des commandes");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'affichage des statistiques: " + e.getMessage());
        }
    }

    @FXML
    private void handleJouerRoulette() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/roulette-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Roulette de la Chance");
            
            RouletteController rouletteController = loader.getController();
            rouletteController.setCommandeController(this);
            
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture de la roulette: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetourAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) commandeTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de revenir à l'accueil : " + e.getMessage());
        }
    }

    public void appliquerReduction(int pourcentageReduction) {
        try {
            if (prixTotalField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez d'abord saisir un prix total");
                return;
            }

            if (montantOriginal == 0.0) {
                montantOriginal = Double.parseDouble(prixTotalField.getText());
            }

            double reduction = montantOriginal * (pourcentageReduction / 100.0);
            double nouveauMontant = montantOriginal - reduction;
            prixTotalField.setText(String.format("%.2f", nouveauMontant));
            
            showAlert("Succès", String.format("Réduction de %d%% appliquée !\nMontant original: %.2f€\nRéduction: %.2f€\nNouveau montant: %.2f€", 
                pourcentageReduction, montantOriginal, reduction, nouveauMontant));
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le montant doit être un nombre valide");
        }
    }

    private void chargerCommandes() {
        try {
            // S'assurer que la connexion est valide
            Connection conn = MyDB.getInstance().getCon();
            if (conn == null) {
                showAlert("Erreur", "La connexion à la base de données est null");
                return;
            }

            // Requête de test pour voir le contenu brut de la table
            String testSql = "SELECT * FROM commande";
            try (Statement stmt = conn.createStatement();
                 ResultSet testRs = stmt.executeQuery(testSql)) {
                
                ResultSetMetaData rsmd = testRs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                
                // Afficher les noms des colonnes
                System.out.println("Colonnes disponibles :");
                for (int i = 1; i <= columnCount; i++) {
                    System.out.println(rsmd.getColumnName(i));
                }
                
                // Afficher les données
                System.out.println("\nContenu de la table :");
                while (testRs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(rsmd.getColumnName(i) + ": " + testRs.getString(i) + " | ");
                    }
                    System.out.println();
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la lecture de la table : " + e.getMessage());
                e.printStackTrace();
            }

            // Requête principale modifiée pour utiliser les noms exacts des colonnes
            String sql = "SELECT c.* FROM commande c";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                
                commandeList.clear(); // Vider la liste avant de la remplir
                
                while (rs.next()) {
                    try {
                        Commande commande = new Commande();
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int columnCount = rsmd.getColumnCount();
                        
                        // Parcourir toutes les colonnes et les afficher
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = rsmd.getColumnName(i);
                            String value = rs.getString(i);
                            System.out.println(columnName + ": " + value);
                            
                            switch (columnName.toLowerCase()) {
                                case "id_commande":
                                    commande.setIdCommande(rs.getInt(i));
                                    break;
                                case "livreur_commande_id":
                                    commande.setLivreurCommandeId(rs.getInt(i));
                                    break;
                                case "statue_commande":
                                    commande.setStatutCommande(rs.getString(i));
                                    break;
                                case "date_commande":
                                    Timestamp ts = rs.getTimestamp(i);
                                    if (ts != null) {
                                        commande.setDateCommande(ts.toLocalDateTime());
                                    }
                                    break;
                                case "prixtotal_commande":
                                    commande.setPrixTotalCommande(rs.getDouble(i));
                                    break;
                                case "modepaiement_commande":
                                    commande.setModePaiementCommande(rs.getString(i));
                                    break;
                            }
                        }
                        commandeList.add(commande);
                    } catch (Exception e) {
                        System.err.println("Erreur lors du traitement de la ligne : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des commandes: " + e.getMessage());
            System.err.println("Exception SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur inattendue: " + e.getMessage());
            System.err.println("Exception générale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        livreurIdComboBox.setValue(null);
        prixTotalField.clear();
        statutComboBox.setValue(null);
        modePaiementComboBox.setValue(null);
        commandeTable.getSelectionModel().clearSelection();
        montantOriginal = 0.0;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void filtrerCommandes() {
        String filterType = filterTypeComboBox.getValue();
        String order = orderComboBox.getValue();
        ObservableList<Commande> filteredList = FXCollections.observableArrayList(commandeList);

        if (filterType.equals("Statut")) {
            if (order.equals("Croissant")) {
                filteredList.sort((c1, c2) -> c1.getStatutCommande().compareTo(c2.getStatutCommande()));
            } else {
                filteredList.sort((c1, c2) -> c2.getStatutCommande().compareTo(c1.getStatutCommande()));
            }
        } else if (filterType.equals("Montant")) {
            if (order.equals("Croissant")) {
                filteredList.sort((c1, c2) -> Double.compare(c1.getPrixTotalCommande(), c2.getPrixTotalCommande()));
            } else {
                filteredList.sort((c1, c2) -> Double.compare(c2.getPrixTotalCommande(), c1.getPrixTotalCommande()));
            }
        }

        commandeTable.setItems(filteredList);
    }
}
