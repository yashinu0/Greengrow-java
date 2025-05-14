package Controlles;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Random;

public class RouletteController {
    @FXML private Label reductionLabel;
    @FXML private Label messageLabel;
    @FXML private Label reductionMessageLabel;
    @FXML private Button tournerButton;
    @FXML private VBox resultatsBox;

    private final int[] reductions = {5, 10, 15, 20, 25, 30};
    private Random random = new Random();
    private Timeline animation;
    private int reductionGagnee;
    private CommandeController commandeController;

    public void setCommandeController(CommandeController controller) {
        this.commandeController = controller;
    }

    @FXML
    private void handleTournerRoulette() {
        tournerButton.setDisable(true);
        messageLabel.setText("La roulette tourne...");
        resultatsBox.setVisible(false);

        // Créer une animation qui change les nombres rapidement
        animation = new Timeline(
            new KeyFrame(Duration.millis(100), e -> {
                reductionLabel.setText(reductions[random.nextInt(reductions.length)] + "%");
            })
        );
        animation.setCycleCount(20); // Nombre de changements avant l'arrêt
        
        animation.setOnFinished(e -> {
            // Sélectionner la réduction finale
            reductionGagnee = reductions[random.nextInt(reductions.length)];
            reductionLabel.setText(reductionGagnee + "%");
            
            // Afficher le résultat
            messageLabel.setText("Vous avez gagné !");
            reductionMessageLabel.setText("Réduction de " + reductionGagnee + "% sur votre commande");
            resultatsBox.setVisible(true);
        });
        
        animation.play();
    }

    @FXML
    private void handleAppliquerReduction() {
        if (commandeController != null) {
            commandeController.appliquerReduction(reductionGagnee);
            // Fermer la fenêtre de la roulette
            ((Stage) tournerButton.getScene().getWindow()).close();
        }
    }
}
