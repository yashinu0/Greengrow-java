package Controlles;

import java.util.List;
import javafx.event.ActionEvent;

import Entites.histaction;
import Entites.utilisateur;
import Services.actionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ListAction {

    @FXML
    private ListView<histaction> actionListView;

    private actionService actionService = new actionService();


    public void setUser(utilisateur selectedUser) {
        List<histaction> actions = actionService.findActionByUserId(selectedUser.getId_user());

        ObservableList<histaction> observableActions = FXCollections.observableArrayList(actions);

        actionListView.setItems(observableActions);

        actionListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(histaction action, boolean empty) {
                super.updateItem(action, empty);

                if (empty || action == null) {
                    setText(null);
                } else {

                    setText(
                            "Type d'action : " + action.getType_action() + "\n" +
                                    "Description d'action : " + action.getDescription_action() + "\n" +
                                    "Date d'action : " + "[" + action.getDate_action() + "]" + "\n"
                    );

                }
            }
        });
    }@FXML
    void effacerfx(ActionEvent event) {
        histaction selectedAction = actionListView.getSelectionModel().getSelectedItem();

        if (selectedAction != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer cette action ?");
            alert.setContentText("Es-tu sûr de vouloir supprimer cette action ?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    actionService.deleteAction(selectedAction);
                    actionListView.getItems().remove(selectedAction);
                }
            });
        } else {
            System.out.println("Aucune action sélectionnée.");
        }
    }
}
