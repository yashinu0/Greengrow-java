package Controlles;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import Entites.utilisateur;
import Services.utilisateurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ListUser {

    @FXML
    private ListView<utilisateur> ListUserFx;

    @FXML
    private TextField nomfx;
    @FXML
    private TextField prenomfx;
    @FXML
    private TextField emailfx;
    @FXML
    private TextField pwdfx;
    @FXML
    private TextField rolefx;
    @FXML
    private TextField adressfx;
    @FXML
    private TextField telfx;

    private utilisateur selectedUser;
    private utilisateurService us = new utilisateurService();
    @FXML
    private ListView<String> actionListView;
    @FXML
    void initialize() {
        List<utilisateur> utilisateurs = us.find();
        ObservableList<utilisateur> items = FXCollections.observableArrayList(utilisateurs);
        ListUserFx.setItems(items);

        ListUserFx.setCellFactory(new Callback<>() {
            @Override
            public ListCell<utilisateur> call(ListView<utilisateur> listView) {
                return new ListCell<>() {
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
                };
            }
        });

        ListUserFx.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedUser = newSelection;
            if (newSelection != null) {
                nomfx.setText(newSelection.getNom_user());
                prenomfx.setText(newSelection.getPrenom_user());
                emailfx.setText(newSelection.getEmail_user());
                pwdfx.setText(newSelection.getMot_de_passe_user());
                rolefx.setText(newSelection.getRole_user());
                adressfx.setText(newSelection.getAdresse_user());
                telfx.setText(newSelection.getTelephone_user());
            }
        });
    }

    @FXML
    void updateFx(ActionEvent event) {
        if (selectedUser != null) {
            // Mise à jour des infos de l'utilisateur sélectionné
            selectedUser.setNom_user(nomfx.getText());
            selectedUser.setPrenom_user(prenomfx.getText());
            selectedUser.setEmail_user(emailfx.getText());
            selectedUser.setMot_de_passe_user(pwdfx.getText());
            selectedUser.setRole_user(rolefx.getText());
            selectedUser.setAdresse_user(adressfx.getText());
            selectedUser.setTelephone_user(telfx.getText());

            us.update(selectedUser);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Utilisateur mis à jour");
            alert.show();
            refreshList();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un utilisateur.");
            alert.show();

        }
    }

    private void refreshList() {
        List<utilisateur> utilisateurs = us.find();
        ObservableList<utilisateur> items = FXCollections.observableArrayList(utilisateurs);
        ListUserFx.setItems(items);
    }

    @FXML
    void suppfx(ActionEvent event) {
        if (selectedUser != null) {
            selectedUser.setNom_user(nomfx.getText());
            selectedUser.setPrenom_user(prenomfx.getText());
            selectedUser.setEmail_user(emailfx.getText());
            selectedUser.setMot_de_passe_user(pwdfx.getText());
            selectedUser.setRole_user(rolefx.getText());
            selectedUser.setAdresse_user(adressfx.getText());
            selectedUser.setTelephone_user(telfx.getText());

            us.delete(selectedUser);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Êtes-vous sûr de vouloir supprimer cet élément ?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                Alert alertinfo = new Alert(Alert.AlertType.INFORMATION);
                alertinfo.setContentText("Utilisateur supprimer avec succee");
                alertinfo.show();
                refreshList();
                System.out.println("Élément supprimé !");

            } else {
                Alert alertinfo = new Alert(Alert.AlertType.INFORMATION);
                alertinfo.setContentText("Suppression annulée");
                alertinfo.show();

            }


        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un utilisateur.");
            alert.show();


        }
    }
    @FXML
        void adfx(ActionEvent event) {
            if (selectedUser != null) {
                boolean isCurrentlyActive = selectedUser.isIs_active();

                selectedUser.setIs_active(!isCurrentlyActive);
                System.out.println(isCurrentlyActive);
                us.update(selectedUser);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                if (selectedUser.isIs_active()) {
                    alert.setContentText("Utilisateur débloqué.");
                } else {
                    alert.setContentText("Utilisateur bloqué.");
                }
                alert.show();

                refreshList();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Veuillez sélectionner un utilisateur.");
                alert.show();
            }
        }
    @FXML
    void Historiquefx(ActionEvent event) {
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un utilisateur.");
            alert.show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListAction.fxml"));
            Parent root = loader.load();

            ListAction controller = loader.getController();

            controller.setUser(selectedUser);

            Stage stage = new Stage();
            stage.setTitle("Historique des actions");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}




