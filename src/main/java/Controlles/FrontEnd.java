package Controlles;

import Entites.Produit;
import Entites.utilisateur;
import Services.ServiceProduit;
import Services.utilisateurService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.io.File;
import javafx.stage.FileChooser;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.awt.Desktop;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

public class FrontEnd {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox homeSection;

    @FXML
    private VBox productsSection;

    @FXML
    private FlowPane productsFlowPane;

    @FXML
    private HBox paginationControls;

    @FXML
    private Button prevPageButton;

    @FXML
    private Button nextPageButton;

    @FXML
    private Label pageInfo;

    @FXML
    private TextField searchBar;

    @FXML
    private Button filterButton;

    @FXML
    private Button sortButton;
    @FXML
    public Label nomLabel;
    @FXML
    public Label prenomLabel;
    private int currentUserId;
    @FXML
    Button loginButton;
    @FXML
    Button logoutButton;
    @FXML
    private VBox userMenuBox;

    @FXML
    Button plusButton;




    @FXML
    private ComboBox<String> filterCondition;

    private ServiceProduit serviceProduit = new ServiceProduit();
    private List<Produit> allProducts;
    private int currentPage = 1;
    private static final int PRODUCTS_PER_PAGE = 3;
    private String currentFilter = "nom";
    private boolean ascendingSort = true;
    private List<Produit> filteredProducts;

    @FXML
    private Button mesReclamationsButton;
    @FXML
    private Button supervisionButton;

    @FXML
    private ImageView avatarImage;

    // Lorsqu'on clique sur "Inscription", charger AjouterUtilisateur.fxml
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Afficher une confirmation avant la déconnexion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Déconnexion");
            alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
            alert.setContentText("Vous devrez vous reconnecter pour accéder à votre profil.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                // Nettoyer la session
                SessionManager.getInstance().clearSession();
                currentUserId = 0;

                // Réinitialiser l'interface
                loginButton.setVisible(true);
                mesReclamationsButton.setVisible(false);
                supervisionButton.setVisible(false);
                plusButton.setVisible(false);
                nomLabel.setText("");
                prenomLabel.setText("");
                nomLabel.setVisible(false);
                prenomLabel.setVisible(false);
                avatarImage.setVisible(false);
                userMenuBox.setVisible(false);

                // Charger la page de login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Connexion");
                stage.show();

                showSuccess("Déconnexion", "Vous avez été déconnecté avec succès.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de la déconnexion: " + e.getMessage());
        }
    }
    @FXML
    void InscFront(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ajouterUtilisateur.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inscription");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Lorsqu'on clique sur "Login", charger login.fxml
    @FXML
    void loginFront(ActionEvent event) {

        try {
            SessionManager.getInstance().clearSession();


            avatarImage.setVisible(false);


            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setCurrentUserId(int id) {
        this.currentUserId = id;
        System.out.println("Current User ID: " + currentUserId);
        boolean isLoggedIn = (currentUserId != 0);
        loginButton.setVisible(!isLoggedIn);
        mesReclamationsButton.setVisible(isLoggedIn);
        supervisionButton.setVisible(isLoggedIn);
        plusButton.setVisible(isLoggedIn);

        // Mettre à jour les labels nom et prénom
        if (isLoggedIn) {
            Services.utilisateurService us = new Services.utilisateurService();
            Entites.utilisateur currentUser = us.findByID(currentUserId);
            if (currentUser != null) {
                nomLabel.setText(currentUser.getNom_user());
                prenomLabel.setText(currentUser.getPrenom_user());
                nomLabel.setVisible(true);
                prenomLabel.setVisible(true);
                
                // Charger l'image par défaut pour l'avatar
                try {
                    Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/icons8-utilisateur-100.png"));
                    avatarImage.setImage(defaultAvatar);
                    avatarImage.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                nomLabel.setText("");
                prenomLabel.setText("");
                nomLabel.setVisible(false);
                prenomLabel.setVisible(false);
                avatarImage.setVisible(false);
            }
        } else {
            nomLabel.setText("");
            prenomLabel.setText("");
            nomLabel.setVisible(false);
            prenomLabel.setVisible(false);
            avatarImage.setVisible(false);
        }
    }
    public void initData(int id) {

    }


    public void setNomfx(String nomUser) {
    }

    public void setPrenomfx(String prenomUser) {
    }

    public void setEmailfx(String emailUser) {
    }

    @FXML
    void FrontProduit(ActionEvent event) {
        try {
            // Hide home section and show products section
            homeSection.setVisible(false);
            productsSection.setVisible(true);
            
            // Load all products from database
            allProducts = serviceProduit.afficher();
            filteredProducts = allProducts; // Initialize filtered products
            
            // Show first page
            showPage(1);
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message
            Label errorLabel = new Label("Erreur lors du chargement des produits");
            productsFlowPane.getChildren().add(errorLabel);
        }
    }

    private void showPage(int pageNumber) {
        // Clear existing products
        productsFlowPane.getChildren().clear();
        
        if (filteredProducts == null) {
            filteredProducts = allProducts;
        }
        
        // Calculate start and end indices
        int startIndex = (pageNumber - 1) * PRODUCTS_PER_PAGE;
        int endIndex = Math.min(startIndex + PRODUCTS_PER_PAGE, filteredProducts.size());
        
        // Add products for current page
        for (int i = startIndex; i < endIndex; i++) {
            Pane productCard = createProductCard(filteredProducts.get(i));
            productsFlowPane.getChildren().add(productCard);
        }
        
        // Update page info
        pageInfo.setText("Page " + pageNumber + " sur " + getTotalPages());
        
        // Update button states
        prevPageButton.setDisable(pageNumber <= 1);
        nextPageButton.setDisable(pageNumber >= getTotalPages());
    }

    private int getTotalPages() {
        if (filteredProducts == null) {
            return (int) Math.ceil((double) allProducts.size() / PRODUCTS_PER_PAGE);
        }
        return (int) Math.ceil((double) filteredProducts.size() / PRODUCTS_PER_PAGE);
    }

    @FXML
    void previousPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            showPage(currentPage);
        }
    }

    @FXML
    void nextPage(ActionEvent event) {
        if (currentPage < getTotalPages()) {
            currentPage++;
            showPage(currentPage);
        }
    }

    private Pane createProductCard(Produit produit) {
        Pane card = new Pane();
        card.setPrefSize(250, 500);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Product Image
        ImageView imageView = new ImageView();
        try {
            String imagePath = produit.getImageProduit();
            if (imagePath != null) {
                imagePath = imagePath.replace("\\", "/");
                imagePath = imagePath.replaceFirst(".*resources", "");
                URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl != null) {
                    Image image = new Image(imageUrl.toString());
                    imageView.setImage(image);
                } else {
                    // Image par défaut si l'URL est invalide
                    imageView.setImage(new Image(getClass().getResource("/images/logo.png").toString()));
                }
            } else {
                // Image par défaut si le chemin est null
                imageView.setImage(new Image(getClass().getResource("/images/logo.png").toString()));
            }
        } catch (Exception e) {
            // Image par défaut en cas d'erreur
            imageView.setImage(new Image(getClass().getResource("/images/logo.png").toString()));
            e.printStackTrace();
        }

        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setLayoutX(25);
        imageView.setLayoutY(20);

        // Product Name
        Label nameLabel = new Label(produit.getNomProduit());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        nameLabel.setLayoutX(25);
        nameLabel.setLayoutY(180);

        // Product Description
        Label descLabel = new Label(produit.getDescriptionProduit());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(200);
        descLabel.setLayoutX(25);
        descLabel.setLayoutY(210);

        // Product Price
        Label priceLabel = new Label(produit.getPrixProduit() + " DT");
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2E8B57; -fx-font-weight: bold;");
        priceLabel.setLayoutX(25);
        priceLabel.setLayoutY(260);

        // Map Button
        Button mapButton = new Button("Voir sur la carte");
        mapButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
        mapButton.setLayoutX(25);
        mapButton.setLayoutY(290);
        mapButton.setPrefWidth(200);
        mapButton.setOnAction(e -> showMapDialog(produit));

        // Order Button
        Button orderButton = new Button("Passer Commande");
        orderButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5;");
        orderButton.setLayoutX(25);
        orderButton.setLayoutY(330);
        orderButton.setPrefWidth(200);
        orderButton.setOnAction(e -> openCommandeView());

        // Complaint Button
        Button complaintButton = new Button("Passer Réclamation");
        complaintButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5;");
        complaintButton.setLayoutX(25);
        complaintButton.setLayoutY(370);
        complaintButton.setPrefWidth(200);
        complaintButton.setOnAction(e -> showComplaintDialog(produit, complaintButton));

        // Export PDF Button
        Button exportPdfButton = new Button("Exporter PDF");
        exportPdfButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-background-radius: 5;");
        exportPdfButton.setLayoutX(25);
        exportPdfButton.setLayoutY(410);
        exportPdfButton.setPrefWidth(200);
        exportPdfButton.setOnAction(e -> exportToPdf(produit));

        // Add all elements to the card
        card.getChildren().addAll(imageView, nameLabel, descLabel, priceLabel, 
                                mapButton, orderButton, complaintButton, exportPdfButton);

        return card;
    }

    private void showMapDialog(Produit produit) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Choisir la plateforme");
        dialog.setHeaderText("Comment souhaitez-vous voir la carte ?");

        ButtonType desktopButton = new ButtonType("Ouvrir sur le bureau", ButtonBar.ButtonData.OK_DONE);
        ButtonType mobileButton = new ButtonType("Scanner QR Code", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(desktopButton, mobileButton, cancelButton);

        dialog.showAndWait().ifPresent(response -> {
            if (response == desktopButton) {
                // Open map in desktop
                openDesktopMap(produit);
            } else if (response == mobileButton) {
                // Show QR code
                showQRCode(produit);
            }
        });
    }

    private void showQRCode(Produit produit) {
        try {
            // Create Google Maps URL with the product's location
            String location = produit.getLocation(); // Assuming this is in format "latitude,longitude"
            String googleMapsUrl = "https://www.google.com/maps?q=" + location;

            // Generate QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(googleMapsUrl, BarcodeFormat.QR_CODE, 200, 200);

            // Convert to BufferedImage
            BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            // Convert to JavaFX Image
            WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

            // Create dialog to display QR code
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("QR Code pour la localisation");
            dialog.setHeaderText("Scannez ce QR code pour voir la localisation sur Google Maps");

            // Create image view for QR code
            ImageView qrImageView = new ImageView(fxImage);
            qrImageView.setFitWidth(200);
            qrImageView.setFitHeight(200);

            // Add QR code to dialog
            StackPane stackPane = new StackPane(qrImageView);
            stackPane.setPadding(new Insets(20));
            dialog.getDialogPane().setContent(stackPane);

            // Add close button
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Show dialog
            dialog.showAndWait();
        } catch (WriterException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la génération du QR code");
            alert.setContentText("Impossible de générer le QR code: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void openDesktopMap(Produit produit) {
        try {
            String location = produit.getLocation();
            String googleMapsUrl = "https://www.google.com/maps?q=" + location;
            
            // Open URL in default browser
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(googleMapsUrl));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture de la carte");
            alert.setContentText("Impossible d'ouvrir Google Maps: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showComplaintDialog(Produit produit, Button button) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/FrontReclamation.fxml"));
            Parent root = loader.load();
            FrontReclamationController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            
            // Passer les informations du produit et de l'utilisateur
            controller.setProductId(produit.getId());
            controller.setProductName(produit.getNomProduit());
            
            Stage stage = (Stage) ((Node) button).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouvelle Réclamation");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir le formulaire de réclamation");
            alert.setContentText("Une erreur est survenue lors de l'ouverture du formulaire.");
            alert.showAndWait();
        }
    }

    private String getWikipediaInfo(String productName) {
        try {
            // Encode the product name for URL
            String encodedName = URLEncoder.encode(productName, StandardCharsets.UTF_8.toString());
            
            // Get Wikipedia page
            String url = "https://fr.wikipedia.org/wiki/" + encodedName;
            org.jsoup.nodes.Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            
            // Get the first paragraph
            Element firstParagraph = doc.select("div.mw-parser-output > p").first();
            if (firstParagraph != null) {
                return firstParagraph.text();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Aucune information supplémentaire trouvée sur Wikipédia.";
    }

    private void exportToPdf(Produit produit) {
        try {
            // Create a new document with page size A4
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
            
            // Get user's desktop path
            String desktopPath = System.getProperty("user.home") + "/Desktop";
            String fileName = produit.getNomProduit().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
            String filePath = desktopPath + "/" + fileName;
            
            // Create PDF file
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Add title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph(produit.getNomProduit(), titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add product image if available
            if (produit.getImageProduit() != null && !produit.getImageProduit().isEmpty()) {
                try {
                    String imagePath = produit.getImageProduit().replace("\\", "/");
                    imagePath = imagePath.replaceFirst(".*resources", "");
                    URL imageUrl = getClass().getResource(imagePath);
                    if (imageUrl != null) {
                        com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(imageUrl);
                        image.scaleToFit(400, 300);
                        image.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                        document.add(image);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Add product details
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            document.add(new Paragraph("Description: " + produit.getDescriptionProduit(), normalFont));
            document.add(new Paragraph("Prix: " + produit.getPrixProduit() + " DT", normalFont));
            document.add(new Paragraph("Quantité disponible: " + produit.getQuantite(), normalFont));
            document.add(new Paragraph("Disponibilité: " + produit.getDisponibilteProduit(), normalFont));
            if (produit.getLocation() != null && !produit.getLocation().isEmpty()) {
                document.add(new Paragraph("Localisation: " + produit.getLocation(), normalFont));
            }

            // Add Wikipedia information
            document.add(new Paragraph("\nInformations supplémentaires de Wikipédia:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
            String wikiInfo = getWikipediaInfo(produit.getNomProduit());
            document.add(new Paragraph(wikiInfo, normalFont));

            // Add footer
            Paragraph footer = new Paragraph("Smart Farming - " + new java.util.Date(), normalFont);
            footer.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();

            // Open the PDF file automatically
            try {
                File pdfFile = new File(filePath);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("PDF généré avec succès");
            alert.setContentText("Le fichier PDF a été généré sur votre bureau et ouvert automatiquement.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la génération du PDF");
            alert.setContentText("Une erreur est survenue lors de la génération du PDF: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void showHome() {
        // Affiche la section Accueil
        homeSection.setVisible(true);
        homeSection.setManaged(true);

        // Masque la section Produits
        productsSection.setVisible(false);
        productsSection.setManaged(false);
    }

    void generateAvatar(String seed) {
        try {
            // Base URL de Robohash
            String imageUrl = "https://robohash.org/" + seed + ".png";

            // Créer une image à partir de l'URL
            Image avatar = new Image(imageUrl);

            // Afficher l'image dans le composant ImageView
            avatarImage.setImage(avatar);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'image Robohash.");
        }
    }
    @FXML
    void initialize() {
        utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            nomLabel.setText(currentUser.getNom_user());
            prenomLabel.setText(currentUser.getPrenom_user());
            currentUserId = currentUser.getId_user();
            // Affiche un bouton supplémentaire

            generateAvatar(currentUser.getNom_user());


            System.out.println("Utilisateur connecté : " + currentUser.getNom_user());
        } else {
            System.out.println("Aucun utilisateur n'est connecté.");
        }

        // Initialize filter conditions
        filterCondition.getItems().addAll("Nom", "Prix", "Description");
        filterCondition.setValue("Nom");
        
        // Add listeners
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterAndDisplayProducts();
        });

        filterCondition.setOnAction(event -> {
            currentFilter = filterCondition.getValue().toLowerCase();
            filterAndDisplayProducts();
        });

        sortButton.setOnAction(event -> {
            ascendingSort = !ascendingSort;
            filterAndDisplayProducts();
        });
        System.out.println(currentUserId);
        if (currentUserId != 0) {
            loginButton.setVisible(false);
            userMenuBox.setVisible(false);
        } else {
            loginButton.setVisible(true);
            userMenuBox.setVisible(false);
        }
    }

    @FXML
    void sortProducts(ActionEvent event) {
        ascendingSort = !ascendingSort;
        filterAndDisplayProducts();
    }

    private void filterAndDisplayProducts() {
        if (allProducts == null) return;

        String searchText = searchBar.getText().toLowerCase();

        // Apply filters and sorting
        filteredProducts = allProducts.stream()
            .filter(produit -> {
                switch (currentFilter) {
                    case "nom":
                        return produit.getNomProduit().toLowerCase().contains(searchText);
                    case "prix":
                        return String.valueOf(produit.getPrixProduit()).contains(searchText);
                    case "description":
                        return produit.getDescriptionProduit().toLowerCase().contains(searchText);
                    default:
                        return true;
                }
            })
            .sorted((p1, p2) -> {
                int comparison = 0;
                switch (currentFilter) {
                    case "nom":
                        comparison = p1.getNomProduit().compareTo(p2.getNomProduit());
                        break;
                    case "prix":
                        comparison = Double.compare(p1.getPrixProduit(), p2.getPrixProduit());
                        break;
                    case "description":
                        comparison = p1.getDescriptionProduit().compareTo(p2.getDescriptionProduit());
                        break;
                }
                return ascendingSort ? comparison : -comparison;
            })
            .collect(Collectors.toList());

        // Reset to first page when filtering
        currentPage = 1;
        showPage(currentPage);
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        try {
            // Vérifier si l'utilisateur est connecté
            if (currentUserId == 0) {
                showError("Erreur", "Vous devez être connecté pour accéder à votre profil.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileUser.fxml"));
            Parent root = loader.load();

            // Get the controller
            ProfileUser controller = loader.getController();

            // Get current user data
            utilisateurService us = new utilisateurService();
            utilisateur currentUser = us.findByID(currentUserId);

            if (currentUser != null) {
                // Set user data in the profile controller
                controller.setCurrentUserId(currentUser.getId_user());
                controller.setNomfx(currentUser.getNom_user());
                controller.setPrenomfx(currentUser.getPrenom_user());
                controller.setEmailfx(currentUser.getEmail_user());
                controller.setPwdfx(currentUser.getMot_de_passe_user());
                controller.setAdressfx(currentUser.getAdresse_user());
                controller.setCodefx(currentUser.getCode_postal_user());
                controller.setTelfx(currentUser.getTelephone_user());
                controller.setVillefx(currentUser.getVille_user());

                // Show the profile page
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Profil utilisateur");
                stage.show();
            } else {
                showError("Erreur", "Impossible de charger les informations du profil.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors du chargement de la page de profil: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handlePlusButton(ActionEvent event) {
        // Inverse la visibilité du menu utilisateur
        boolean currentlyVisible = userMenuBox.isVisible();
        userMenuBox.setVisible(!currentlyVisible);
    }

    @FXML
    private void handleMesReclamations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/ReclamationUser.fxml"));
            Parent root = loader.load();
            
            // Passer l'ID de l'utilisateur au contrôleur
            ReclamationUserController controller = loader.getController();
            controller.setCurrentUserId(currentUserId);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupervision(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Rendu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleContactezNous(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/FrontFeed.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la nouvelle vue
            Controlles.FrontFeedController controller = loader.getController();

            // Passer l'ID utilisateur courant au contrôleur
            controller.setCurrentUserId(currentUserId);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Contactez-nous");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void accueilFront(ActionEvent event) {
        // Show home section and hide products section
        homeSection.setVisible(true);
        homeSection.setManaged(true);
        productsSection.setVisible(false);
        productsSection.setManaged(false);
    }

    private void openCommandeView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/commande-view.fxml"));
            Parent root = loader.load();
            Stage mainStage = (Stage) productsFlowPane.getScene().getWindow();
            mainStage.setScene(new Scene(root));
            mainStage.setTitle("Gestion des Commandes");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la vue Commande");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
