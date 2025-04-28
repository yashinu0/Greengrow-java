package Controlles;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Entites.utilisateur;
import Services.utilisateurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ListUser {

    @FXML private ListView<utilisateur> ListUserFx;
    @FXML private TextField nomfx;
    @FXML private TextField prenomfx;
    @FXML private TextField emailfx;
    @FXML private TextField pwdfx;
    @FXML private TextField rolefx;
    @FXML private TextField adressfx;
    @FXML private TextField telfx;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortFieldCombo;
    @FXML private ComboBox<String> sortOrderCombo;
    @FXML private PieChart pieChart;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private utilisateur selectedUser;
    private final utilisateurService us = new utilisateurService();
    private ObservableList<utilisateur> masterList;
    private int currentPage  = 1;
    private final int itemsPerPage = 10;
    private int totalItems, totalPages;

    @FXML
    void initialize() {
        // Charger les utilisateurs
        masterList = FXCollections.observableArrayList(us.find());

        // Pagination
        prevButton.setOnAction(e -> goToPreviousPage());
        nextButton.setOnAction(e -> goToNextPage());
        updatePagination();

        // Initialiser tri et recherche
        sortFieldCombo.getItems().addAll("Nom", "Prénom", "Email", "Role");
        sortOrderCombo.getItems().addAll("Ascendant", "Descendant");
        sortFieldCombo.getSelectionModel().selectFirst();
        sortOrderCombo.getSelectionModel().selectFirst();
        searchField.textProperty().addListener((obs, o, n) -> updateList());
        sortFieldCombo.valueProperty().addListener((obs, o, n) -> updateList());
        sortOrderCombo.valueProperty().addListener((obs, o, n) -> updateList());

        // Afficher la page et le chart
        updatePageLabel();
        updatePieChart();

        // Charger la vue détaillée des actions sur sélection
        updateList();
    }

    private void updatePieChart() {
        // Calcule la répartition des rôles dynamiquement
        Map<String, Long> counts = masterList.stream()
                .collect(Collectors.groupingBy(utilisateur::getRole_user, Collectors.counting()));

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        counts.forEach((role, count) -> data.add(new PieChart.Data(role + " (" + count + ")", count)));

        pieChart.setData(data);
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
    }

    private void updatePagination() {
        totalItems = masterList.size();
        totalPages = (int)Math.ceil((double)totalItems / itemsPerPage);

        int from = (currentPage - 1) * itemsPerPage;
        int to   = Math.min(from + itemsPerPage, totalItems);
        ListUserFx.setItems(FXCollections.observableArrayList(masterList.subList(from, to)));

        updatePageLabel();
    }

    private void updatePageLabel() {
        pageLabel.setText("Page " + currentPage + " sur " + (totalPages == 0 ? 1 : totalPages));
    }

    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    private void updateList() {
        String term = searchField.getText().toLowerCase();
        Stream<utilisateur> stream = masterList.stream()
                .filter(u -> u.getNom_user().toLowerCase().contains(term)
                        || u.getPrenom_user().toLowerCase().contains(term)
                        || u.getEmail_user().toLowerCase().contains(term)
                        || u.getRole_user().toLowerCase().contains(term));

        Comparator<utilisateur> comp = getComparator(
                sortFieldCombo.getValue(), sortOrderCombo.getValue()
        );

        List<utilisateur> filtered = stream.sorted(comp).collect(Collectors.toList());
        masterList.setAll(filtered);        // Met à jour la liste maître
        currentPage = 1;                    // Retour à la première page
        updatePagination();                 // Rafraîchir ListView
        updatePieChart();                   // Rafraîchir PieChart
        configureListCellFactory();
    }

    private void configureListCellFactory() {
        ListUserFx.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(utilisateur u, boolean empty) {
                super.updateItem(u, empty);
                if (empty || u == null) {
                    setGraphic(null);
                } else {
                    HBox row = new HBox(10);
                    Label nom = new Label(u.getNom_user());
                    nom.setPrefWidth(80);

                    Label prenom = new Label(u.getPrenom_user());
                    prenom.setPrefWidth(80);

                    Label email = new Label(u.getEmail_user());
                    email.setPrefWidth(140);

                    Label mdp = new Label(u.getMot_de_passe_user());
                    mdp.setPrefWidth(100);

                    Label role = new Label(u.getRole_user());
                    role.setPrefWidth(120);

                    Label adresse = new Label(u.getAdresse_user());
                    adresse.setPrefWidth(160);

                    Label tel = new Label(u.getTelephone_user());
                    tel.setPrefWidth(100);

                    row.getChildren().addAll(nom, prenom, email, mdp, role, adresse, tel);
                    setGraphic(row);
                }
            }
        });

        ListUserFx.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            selectedUser = n;
            if (n != null) {
                nomfx.setText(n.getNom_user());
                prenomfx.setText(n.getPrenom_user());
                emailfx.setText(n.getEmail_user());
                pwdfx.setText(n.getMot_de_passe_user());
                rolefx.setText(n.getRole_user());
                adressfx.setText(n.getAdresse_user());
                telfx.setText(n.getTelephone_user());
            }
        });
    }

    private Comparator<utilisateur> getComparator(String field, String order) {
        Comparator<utilisateur> cmp;
        switch (field) {
            case "Prénom": cmp = Comparator.comparing(utilisateur::getPrenom_user); break;
            case "Email":  cmp = Comparator.comparing(utilisateur::getEmail_user);  break;
            case "Role":   cmp = Comparator.comparing(utilisateur::getRole_user);   break;
            default:       cmp = Comparator.comparing(utilisateur::getNom_user);
        }
        return "Descendant".equals(order) ? cmp.reversed() : cmp;
    }

    @FXML
    void updateFx(ActionEvent e) {
        if (selectedUser != null) {
            selectedUser.setNom_user(nomfx.getText());
            selectedUser.setPrenom_user(prenomfx.getText());
            selectedUser.setEmail_user(emailfx.getText());
            selectedUser.setMot_de_passe_user(pwdfx.getText());
            selectedUser.setRole_user(rolefx.getText());
            selectedUser.setAdresse_user(adressfx.getText());
            selectedUser.setTelephone_user(telfx.getText());
            us.update(selectedUser);
            new Alert(Alert.AlertType.INFORMATION, "Utilisateur mis à jour !").show();
            refreshAll();
        } else {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un utilisateur.").show();
        }
    }

    @FXML
    void suppfx(ActionEvent e) {
        if (selectedUser != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Êtes-vous sûr de vouloir supprimer cet utilisateur ?", ButtonType.OK, ButtonType.CANCEL);
            confirm.showAndWait().ifPresent(b -> {
                if (b == ButtonType.OK) {
                    us.delete(selectedUser);
                    new Alert(Alert.AlertType.INFORMATION, "Utilisateur supprimé avec succès.").show();
                    refreshAll();
                }
            });
        } else {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un utilisateur.").show();
        }
    }

    @FXML
    void adfx(ActionEvent e) {
        if (selectedUser != null) {
            selectedUser.setIs_active(!selectedUser.isIs_active());
            us.update(selectedUser);
            new Alert(Alert.AlertType.INFORMATION,
                    selectedUser.isIs_active() ? "Utilisateur débloqué." : "Utilisateur bloqué.")
                    .show();
            refreshAll();
        } else {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un utilisateur.").show();
        }
    }

    @FXML
    void Historiquefx(ActionEvent e) {
        if (selectedUser == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner un utilisateur.").show();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListAction.fxml"));
            Parent root = loader.load();
            ListAction ctrl = loader.getController();
            ctrl.setUser(selectedUser);
            Stage stage = new Stage();
            stage.setTitle("Historique des actions");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void refreshAll() {
        masterList.setAll(us.find());
        updateList();
    }
}
