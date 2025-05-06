package Controlles;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import Entities.Alerte;
import Entities.Rendu;
import Services.AlerteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AlerteController implements Initializable {
    private static class PlantNeeds {
        final int minLight;
        final int maxLight;
        final int minHum;
        final int maxHum;
        final int minTemp;
        final int maxTemp;
        final int minWater;
        final int maxWater;

        PlantNeeds(int minLight, int maxLight, int minHum, int maxHum, int minTemp, int maxTemp, int minWater, int maxWater) {
            this.minLight = minLight;
            this.maxLight = maxLight;
            this.minHum = minHum;
            this.maxHum = maxHum;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.minWater = minWater;
            this.maxWater = maxWater;
        }
    }

    private static final Map<String, PlantNeeds> PLANT_TYPE_NEEDS = new HashMap<>();
    static {
        PLANT_TYPE_NEEDS.put("Succulents", new PlantNeeds(50, 80, 10, 30, 18, 30, 20, 100));
        PLANT_TYPE_NEEDS.put("Flowering Plants", new PlantNeeds(40, 80, 40, 70, 18, 28, 50, 200));
        PLANT_TYPE_NEEDS.put("Herbs", new PlantNeeds(40, 70, 40, 70, 15, 25, 30, 150));
        PLANT_TYPE_NEEDS.put("Vegetables", new PlantNeeds(40, 80, 50, 80, 16, 28, 50, 200));
        PLANT_TYPE_NEEDS.put("Shrubs", new PlantNeeds(30, 70, 30, 60, 10, 30, 50, 200));
    }

    @FXML private VBox alertListContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        styleNavBar();
        initializeAlertCards();
    }

    private void styleNavBar() {
        alertListContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                javafx.scene.Scene scene = alertListContainer.getScene();
                if (scene != null && scene.getRoot() instanceof BorderPane) {
                    javafx.scene.Node navBar = ((BorderPane) scene.getRoot()).getTop();
                    if (navBar instanceof HBox) {
                        HBox topBar = (HBox) navBar;
                        topBar.setStyle("-fx-background-color: linear-gradient(to right, #14532d, #1e7a3a); -fx-padding: 18px 30px; -fx-background-radius: 16px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10,0,0,2);");
                        for (javafx.scene.Node node : topBar.getChildren()) {
                            if (node instanceof HBox) {
                                ((HBox) node).setSpacing(20);
                                for (javafx.scene.Node btn : ((HBox) node).getChildren()) {
                                    if (btn instanceof Button) {
                                        Button button = (Button) btn;
                                        if ("Home".equals(button.getText())) {
                                            button.setStyle("-fx-background-color: #fff; -fx-text-fill: #14532d; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8;");
                                            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #14532d; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8;"));
                                            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #fff; -fx-text-fill: #14532d; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8;"));
                                        } else if ("Alerte".equals(button.getText())) {
                                            button.setStyle("-fx-background-color: #228B22; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8; -fx-border-color: #fff; -fx-border-width: 2px; -fx-border-radius: 8;");
                                        } else {
                                            button.setStyle("-fx-background-color: #fff; -fx-text-fill: #14532d; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8;");
                                            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #14532d; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8;"));
                                            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #fff; -fx-text-fill: #14532d; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8;"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void showAlerts() {
        alertListContainer.getChildren().clear();
        try {
            AlerteService alerteService = new AlerteService();
            List<Alerte> alertes = alerteService.getAllAlertes();
            if (alertes.isEmpty()) {
                Label noAlerts = new Label("No plants/alerts found.");
                noAlerts.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #185a9d; -fx-padding: 40 0 20 0;");
                alertListContainer.getChildren().add(noAlerts);
            } else {
                FlowPane flowPane = new FlowPane();
                flowPane.setHgap(30);
                flowPane.setVgap(30);
                flowPane.setPadding(new Insets(20, 0, 20, 0));
                flowPane.setPrefWrapLength(1100);
                flowPane.setAlignment(Pos.CENTER);
                
                for (Alerte alerte : alertes) {
                    VBox card = new VBox(8);
                    card.setPadding(new Insets(18));
                    card.setSpacing(6);
                    card.setMaxWidth(420);
                    card.setMinWidth(340);
                    card.setAlignment(Pos.TOP_LEFT);
                    String cardStyle = "-fx-background-color: #fff; -fx-border-color: #43cea2; -fx-border-width: 2px; -fx-border-radius: 16; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(24,90,157,0.10), 8,0,0,2);";
                    if ("critical condition".equalsIgnoreCase(alerte.getNiveau_urgence_alerte())) {
                        cardStyle = "-fx-background-color: #ffcccc; -fx-border-color: #d32f2f; -fx-border-width: 2px; -fx-border-radius: 16; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(211,47,47,0.15), 8,0,0,2);";
                    } else if ("safe".equalsIgnoreCase(alerte.getNiveau_urgence_alerte())) {
                        cardStyle = "-fx-background-color: #d0f5e8; -fx-border-color: #43cea2; -fx-border-width: 2px; -fx-border-radius: 16; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(67,206,162,0.10), 8,0,0,2);";
                    }
                    card.setStyle(cardStyle);
                    Rendu plant = alerte.getRendu();
                    String plantName = plant != null ? plant.getMessage_rendu() : "Unknown";
                    String plantType = plant != null ? plant.getType_rendu() : "Unknown";
                    Label name = new Label("🌱 " + plantName);
                    name.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #185a9d;");
                    Label type = new Label("Type: " + plantType);
                    type.setStyle("-fx-font-size: 15px; -fx-text-fill: #555;");
                    Label urgency = new Label("Urgency: " + alerte.getNiveau_urgence_alerte());
                    urgency.setStyle("-fx-font-size: 15px; -fx-text-fill: " + ("critical condition".equalsIgnoreCase(alerte.getNiveau_urgence_alerte()) ? "#d32f2f" : "#43cea2") + "; -fx-font-weight: bold;");
                    
                    // Add detailed message based on condition
                    Label detailedMessage = new Label();
                    detailedMessage.setWrapText(true);
                    detailedMessage.setMaxWidth(340);
                    detailedMessage.setStyle("-fx-font-size: 14px; -fx-padding: 8 0;");
                    
                    if ("safe".equalsIgnoreCase(alerte.getNiveau_urgence_alerte())) {
                        detailedMessage.setText("✅ Your plant is healthy and well-maintained. Keep up the good work!");
                        detailedMessage.setStyle("-fx-text-fill: #43cea2; -fx-font-size: 14px; -fx-padding: 8 0;");
                    } else {
                        // Get plant data from preferences
                        Preferences prefs = Preferences.userNodeForPackage(getClass());
                        String key = "plant_" + plantName + "_" + plantType + "_" + plant.getDate_envoi_rendu();
                        String value = prefs.get(key, null);
                        StringBuilder message = new StringBuilder("⚠️ Critical Care Needed:\n");
                        
                        if (value != null) {
                            String[] parts = value.split(",");
                            PlantNeeds needs = PLANT_TYPE_NEEDS.getOrDefault(plantType, new PlantNeeds(0,100,0,100,0,100,0,10000));
                            int lum = Integer.parseInt(parts[0]);
                            int hum = Integer.parseInt(parts[1]);
                            int temp = Integer.parseInt(parts[2]);
                            int water = Integer.parseInt(parts[3]);
                            
                            if (lum < needs.minLight) {
                                message.append("• Light is too low (").append(lum).append("%). Move the plant to a brighter location.\n");
                            } else if (lum > needs.maxLight) {
                                message.append("• Light is too high (").append(lum).append("%). Provide some shade or move to a less bright spot.\n");
                            }
                            if (hum < needs.minHum) {
                                message.append("• Humidity is too low (").append(hum).append("%). Consider using a humidifier or misting the plant.\n");
                            } else if (hum > needs.maxHum) {
                                message.append("• Humidity is too high (").append(hum).append("%). Improve ventilation or move to a drier area.\n");
                            }
                            if (temp < needs.minTemp) {
                                message.append("• Temperature is too low (").append(temp).append("°C). Move to a warmer location.\n");
                            } else if (temp > needs.maxTemp) {
                                message.append("• Temperature is too high (").append(temp).append("°C). Move to a cooler spot.\n");
                            }
                            if (water < needs.minWater) {
                                message.append("• Water level is too low (").append(water).append("ml). Increase watering frequency.\n");
                            } else if (water > needs.maxWater) {
                                message.append("• Water level is too high (").append(water).append("ml). Reduce watering and ensure proper drainage.\n");
                            }
                        } else {
                            message.append("No sensor data available. Please check the plant's condition manually.");
                        }
                        
                        detailedMessage.setText(message.toString());
                        detailedMessage.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px; -fx-padding: 8 0;");
                    }
                    
                    Button deleteAlerteBtn = new Button("Delete Alert");
                    deleteAlerteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 18; -fx-font-size: 15px;");
                    deleteAlerteBtn.setOnAction(e -> {
                        try {
                            alerteService.deleteAlerte(alerte.getId_alerte());
                            showAlerts();
                        } catch (Exception ex) {
                            // Optionally show an error dialog
                        }
                    });
                    card.getChildren().addAll(name, type, urgency, detailedMessage, deleteAlerteBtn);
                    flowPane.getChildren().add(card);
                }
                alertListContainer.getChildren().add(flowPane);
            }
        } catch (Exception e) {
            Label err = new Label("Error loading alerts.");
            alertListContainer.getChildren().add(err);
        }
    }

    @FXML
    private void handleRenduButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Rendu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) alertListContainer.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("GreenGrow");
            stage.show();
        } catch (Exception e) {
            // Optionally show an error dialog
        }
    }

    @FXML
    private void handleAlerteButton() {
        // Already on Alerte page, do nothing
    }

    @FXML
    private void handleHomeButton() {
        // Do nothing for now
    }

    private void initializeAlertCards() {
        showAlerts();
    }
} 