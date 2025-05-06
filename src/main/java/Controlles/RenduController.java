package Controlles;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import Entities.Rendu;
import Services.AlerteService;
import Services.RenduService;
import Services.WeatherService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RenduController implements Initializable {

    @FXML private FlowPane plantListContainer;
    @FXML private Button addPlantButton;
    @FXML private VBox todoListContainer;

    private Label todayTimeLabel; // Store reference for live update
    private TodoListController todoListController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        plantListContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                styleNavBar();
                showPlantCards();
                initializeWeatherDisplay();
                initializeTodoList();
            }
        });
    }

    private void initializeTodoList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/TodoListBoard.fxml"));
            Parent todoListBoard = loader.load();
            todoListController = loader.getController();
            
            // Add the todo list board to the container
            if (todoListContainer != null) {
                todoListContainer.getChildren().clear();
                todoListContainer.getChildren().add(todoListBoard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void styleNavBar() {
        javafx.scene.Scene scene = plantListContainer.getScene();
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
                                    button.setStyle("-fx-background-color: #fff; -fx-text-fill: #14532d; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(67,206,162,0.15), 4,0,0,1);");
                                    button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #14532d; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(67,206,162,0.15), 4,0,0,1);"));
                                    button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #fff; -fx-text-fill: #14532d; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(67,206,162,0.15), 4,0,0,1);"));
                                } else if ("Rendu".equals(button.getText())) {
                                    button.setStyle("-fx-background-color: #228B22; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(67,206,162,0.15), 4,0,0,1); -fx-border-color: #fff; -fx-border-width: 2px; -fx-border-radius: 8;");
                                } else {
                                    button.setStyle("-fx-background-color: #fff; -fx-text-fill: #14532d; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(67,206,162,0.15), 4,0,0,1);");
                                    button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #14532d; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(67,206,162,0.15), 4,0,0,1);"));
                                    button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #fff; -fx-text-fill: #14532d; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 6 16; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(67,206,162,0.15), 4,0,0,1);"));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void showAddPlantForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddPlantForm.fxml"));
            Parent root = loader.load();
            AddPlantController addPlantController = loader.getController();
            addPlantController.setTodoListController(todoListController);

            Stage stage = new Stage();
            stage.setTitle("Add New Plant");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> showPlantCards());
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load the add plant form", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRenduButton() {
        // Already on Rendu page, do nothing
    }

    @FXML
    private void handleAlerteButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Alerte.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) addPlantButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("GreenGrow - Alerts");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load the alerts page", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleHomeButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load FrontEnd view: " + e.getMessage());
            showAlert("Error", "Could not return to FrontEnd view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private static class PlantNeeds {
        int minLight, maxLight, minHum, maxHum, minTemp, maxTemp, minWater, maxWater;
        PlantNeeds(int minL, int maxL, int minH, int maxH, int minT, int maxT, int minW, int maxW) {
            minLight = minL; maxLight = maxL; minHum = minH; maxHum = maxH; minTemp = minT; maxTemp = maxT; minWater = minW; maxWater = maxW;
        }
    }
    private static final Map<String, PlantNeeds> PLANT_TYPE_NEEDS = new HashMap<>();
    static {
        PLANT_TYPE_NEEDS.put("Flowering Plants", new PlantNeeds(40, 80, 40, 70, 18, 28, 50, 200));
        PLANT_TYPE_NEEDS.put("Succulents", new PlantNeeds(50, 80, 10, 30, 18, 30, 20, 100));
        PLANT_TYPE_NEEDS.put("Herbs", new PlantNeeds(40, 70, 40, 70, 15, 25, 30, 150));
        PLANT_TYPE_NEEDS.put("Vegetables", new PlantNeeds(40, 80, 50, 80, 16, 28, 50, 200));
        PLANT_TYPE_NEEDS.put("Fruits", new PlantNeeds(50, 80, 50, 80, 18, 30, 50, 200));
        PLANT_TYPE_NEEDS.put("Trees", new PlantNeeds(30, 70, 30, 60, 10, 30, 50, 200));
        PLANT_TYPE_NEEDS.put("Shrubs", new PlantNeeds(30, 70, 30, 60, 10, 30, 50, 200));
    }

    private void showPlantCards() {
        plantListContainer.getChildren().clear();
        initializeWeatherDisplay();
        RenduService renduService = new RenduService();
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        try {
            List<Rendu> plants = renduService.getAllRendus();
            if (plants.isEmpty()) {
                Label noPlants = new Label("No Plants Found");
                noPlants.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #185a9d; -fx-padding: 40 0 20 0;");
                plantListContainer.getChildren().add(noPlants);
            } else {
                // Create warning bar container
                VBox warningBar = new VBox();
                warningBar.setStyle("-fx-background-color: #ffebee; -fx-border-color: #d32f2f; -fx-border-width: 2px; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 18 0 18 0; -fx-spacing: 8; margin: 0 auto;");
                warningBar.setMaxWidth(1100);
                warningBar.setAlignment(Pos.CENTER);
                
                // Count critical plants and collect their names
                List<String> criticalPlants = new ArrayList<>();
                for (Rendu plant : plants) {
                    String key = "plant_" + plant.getMessage_rendu() + "_" + plant.getType_rendu() + "_" + plant.getDate_envoi_rendu();
                    String value = prefs.get(key, null);
                    if (value != null) {
                        String[] parts = value.split(",");
                        PlantNeeds needs = PLANT_TYPE_NEEDS.getOrDefault(plant.getType_rendu(), new PlantNeeds(0,100,0,100,0,100,0,10000));
                        int lum = Integer.parseInt(parts[0]);
                        int hum = Integer.parseInt(parts[1]);
                        int temp = Integer.parseInt(parts[2]);
                        int water = Integer.parseInt(parts[3]);
                        
                        if (lum < needs.minLight || lum > needs.maxLight ||
                            hum < needs.minHum || hum > needs.maxHum ||
                            temp < needs.minTemp || temp > needs.maxTemp ||
                            water < needs.minWater || water > needs.maxWater) {
                            criticalPlants.add(plant.getMessage_rendu());
                        }
                    }
                }
                
                // Add warning bar if there are critical plants
                if (!criticalPlants.isEmpty()) {
                    Label warningTitle = new Label("⚠️ Critical Condition Alert");
                    warningTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #d32f2f;");
                    
                    Label warningText = new Label("You have " + criticalPlants.size() + " plant(s) in critical condition. Please check the Alerte page for more information.");
                    warningText.setStyle("-fx-font-size: 15px; -fx-text-fill: #d32f2f;");
                    
                    Label plantNames = new Label("Affected plants: " + String.join(", ", criticalPlants));
                    plantNames.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                    plantNames.setWrapText(true);
                    plantNames.setMaxWidth(760);
                    
                    warningBar.getChildren().addAll(warningTitle, warningText, plantNames);
                    // Center warningBar in a full-width HBox
                    HBox warningBox = new HBox(warningBar);
                    warningBox.setAlignment(Pos.CENTER);
                    warningBox.setPadding(new Insets(0, 0, 20, 0));
                    plantListContainer.getChildren().add(warningBox);
                }
                
                FlowPane flowPane = new FlowPane();
                flowPane.setHgap(30);
                flowPane.setVgap(30);
                flowPane.setPadding(new Insets(20, 0, 20, 0));
                flowPane.setPrefWrapLength(1100);
                flowPane.setAlignment(Pos.CENTER);
                
                for (Rendu plant : plants) {
                    VBox card = new VBox(8);
                    card.setPadding(new Insets(18));
                    card.setSpacing(6);
                    card.setMaxWidth(380);
                    card.setMinWidth(320);
                    card.setAlignment(Pos.TOP_LEFT);
                    String cardStyle;
                    String key = "plant_" + plant.getMessage_rendu() + "_" + plant.getType_rendu() + "_" + plant.getDate_envoi_rendu();
                    String value = prefs.get(key, null);
                    Label name = new Label("🌱 " + plant.getMessage_rendu());
                    name.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #185a9d;");
                    Label type = new Label("Type: " + plant.getType_rendu());
                    type.setStyle("-fx-font-size: 15px; -fx-text-fill: #555;");
                    Label date = new Label("Date Added: " + plant.getDate_envoi_rendu());
                    date.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
                    Label lumLabel = new Label();
                    lumLabel.setStyle("-fx-text-fill: #222; -fx-font-size: 15px; -fx-font-weight: 600;");
                    Label humLabel = new Label();
                    humLabel.setStyle("-fx-text-fill: #222; -fx-font-size: 15px; -fx-font-weight: 600;");
                    Label tempLabel = new Label();
                    tempLabel.setStyle("-fx-text-fill: #222; -fx-font-size: 15px; -fx-font-weight: 600;");
                    Label waterLabel = new Label();
                    waterLabel.setStyle("-fx-text-fill: #222; -fx-font-size: 15px; -fx-font-weight: 600;");
                    Label locationLabel = new Label();
                    locationLabel.setStyle("-fx-text-fill: #222; -fx-font-size: 15px; -fx-font-weight: 600;");
                    Label statusLabel = new Label();
                    boolean outOfRange = false;
                    String urgency = "safe";
                    if (value != null) {
                        String[] parts = value.split(",");
                        lumLabel.setText("💡 Light: " + parts[0] + "%");
                        humLabel.setText("💧 Humidity: " + parts[1] + "%");
                        tempLabel.setText("🌡️ Temperature: " + parts[2] + "°C");
                        waterLabel.setText("🚿 Water: " + parts[3] + " ml");
                        locationLabel.setText("🏠 Location: " + (parts.length > 4 ? parts[4] : "N/A"));
                        PlantNeeds needs = PLANT_TYPE_NEEDS.getOrDefault(plant.getType_rendu(), new PlantNeeds(0,100,0,100,0,100,0,10000));
                        int lum = Integer.parseInt(parts[0]);
                        int hum = Integer.parseInt(parts[1]);
                        int temp = Integer.parseInt(parts[2]);
                        int water = Integer.parseInt(parts[3]);
                        
                        if (lum < needs.minLight || lum > needs.maxLight ||
                            hum < needs.minHum || hum > needs.maxHum ||
                            temp < needs.minTemp || temp > needs.maxTemp ||
                            water < needs.minWater || water > needs.maxWater) {
                            outOfRange = true;
                            urgency = "critical condition";
                        }
                        
                        if (outOfRange) {
                            statusLabel.setText("Status: Critical - Please check the Alerte page");
                            statusLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                        } else {
                            statusLabel.setText("Status: Safe");
                            statusLabel.setStyle("-fx-text-fill: #43cea2; -fx-font-weight: bold;");
                        }
                    } else {
                        statusLabel.setText("Status: No data available");
                        statusLabel.setStyle("-fx-text-fill: #888; -fx-font-weight: bold;");
                    }
                    
                    card.getChildren().addAll(name, type, date, lumLabel, humLabel, tempLabel, waterLabel, locationLabel, statusLabel);
                    card.setStyle("-fx-background-color: #fff; -fx-border-color: " + (outOfRange ? "#d32f2f" : "#43cea2") + "; -fx-border-width: 2px; -fx-border-radius: 16; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(24,90,157,0.10), 8,0,0,2);");
                    
                    // Add recommendations button
                    Button recBtn = new Button("Show Recommendations");
                    recBtn.setStyle("-fx-background-color: #43cea2; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 18; -fx-font-size: 15px;");
                    recBtn.setOnAction(e -> showRecommendations(plant.getType_rendu()));
                    
                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 18; -fx-font-size: 15px; -fx-effect: dropshadow(gaussian, rgba(244,67,54,0.10), 4,0,0,1);");
                    deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 18; -fx-font-size: 15px; -fx-effect: dropshadow(gaussian, rgba(244,67,54,0.18), 4,0,0,1);"));
                    deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 18; -fx-font-size: 15px; -fx-effect: dropshadow(gaussian, rgba(244,67,54,0.10), 4,0,0,1);"));
                    deleteBtn.setOnAction(e -> {
                        try {
                            AlerteService alerteService = new AlerteService();
                            List<Entities.Alerte> alertes = alerteService.getAllAlertes();
                            for (Entities.Alerte alerte : alertes) {
                                if (alerte.getRendu() != null && alerte.getRendu().getId_rendu() == plant.getId_rendu()) {
                                    alerteService.deleteAlerte(alerte.getId_alerte());
                                }
                            }
                            renduService.deleteRendu(plant.getId_rendu());
                            prefs.remove(key);
                            showPlantCards();
                        } catch (Exception ex) {
                            showAlert("Error", "Could not delete plant", Alert.AlertType.ERROR);
                        }
                    });
                    card.getChildren().addAll(recBtn, deleteBtn);
                    flowPane.getChildren().add(card);
                }
                plantListContainer.getChildren().add(flowPane);
            }
        } catch (Exception e) {
            showAlert("Error", "Could not load plants", Alert.AlertType.ERROR);
        }
    }

    private void showRecommendations(String plantType) {
        StringBuilder msg = new StringBuilder();
        switch (plantType) {
            case "Succulents":
                msg.append("\u2728 Succulents\n");
                msg.append("Succulents are drought-tolerant plants that thrive in bright, indirect sunlight.\n\n");
                msg.append("\uD83D\uDCA1 Best values:\n");
                msg.append("- Light: 80–100%\n- Humidity: 10–30%\n- Temperature: 18–30°C\n- Water: 50–200ml\n\n");
                msg.append("\uD83D\uDC4D Tips:\n");
                msg.append("- Place your succulent near a sunny window, but avoid harsh midday sun.\n");
                msg.append("- Let the soil dry out completely between waterings.\n");
                msg.append("- Use well-draining soil and a pot with drainage holes.\n");
                msg.append("- Avoid overwatering—succulents are sensitive to root rot!\n\n");
                msg.append("\u26A0\uFE0F Avoid:\n");
                msg.append("- High humidity and standing water.\n- Cold drafts or temperatures below 10°C.\n");
                break;
            case "Flowering Plants":
                msg.append("\u2728 Flowering Plants\n");
                msg.append("These plants reward you with beautiful blooms when cared for properly.\n\n");
                msg.append("\uD83D\uDCA1 Best values:\n");
                msg.append("- Light: 60–100%\n- Humidity: 40–70%\n- Temperature: 18–28°C\n- Water: 200–600ml\n\n");
                msg.append("\uD83D\uDC4D Tips:\n");
                msg.append("- Provide plenty of bright, indirect light.\n");
                msg.append("- Water when the top inch of soil is dry.\n");
                msg.append("- Fertilize during the growing season for more blooms.\n\n");
                msg.append("\u26A0\uFE0F Avoid:\n");
                msg.append("- Overwatering and soggy soil.\n- Placing in dark corners.\n");
                break;
            case "Herbs":
                msg.append("\u2728 Herbs\n");
                msg.append("Herbs love sunlight and regular harvesting.\n\n");
                msg.append("\uD83D\uDCA1 Best values:\n");
                msg.append("- Light: 50–90%\n- Humidity: 40–70%\n- Temperature: 15–25°C\n- Water: 100–400ml\n\n");
                msg.append("\uD83D\uDC4D Tips:\n");
                msg.append("- Place herbs on a sunny windowsill.\n");
                msg.append("- Pinch off leaves regularly to encourage growth.\n");
                msg.append("- Water when the soil feels dry to the touch.\n\n");
                msg.append("\u26A0\uFE0F Avoid:\n");
                msg.append("- Letting soil stay wet for long periods.\n- Lack of sunlight.\n");
                break;
            case "Vegetables":
                msg.append("\u2728 Vegetables\n");
                msg.append("Vegetables need consistent care for a bountiful harvest.\n\n");
                msg.append("\uD83D\uDCA1 Best values:\n");
                msg.append("- Light: 60–100%\n- Humidity: 50–80%\n- Temperature: 16–28°C\n- Water: 200–800ml\n\n");
                msg.append("\uD83D\uDC4D Tips:\n");
                msg.append("- Ensure at least 6 hours of sunlight daily.\n");
                msg.append("- Water deeply but less frequently.\n");
                msg.append("- Mulch to retain soil moisture.\n\n");
                msg.append("\u26A0\uFE0F Avoid:\n");
                msg.append("- Watering leaves directly.\n- Letting soil dry out completely.\n");
                break;
            case "Fruits":
                msg.append("\u2728 Fruits\n");
                msg.append("Fruit plants need warmth and regular feeding.\n\n");
                msg.append("\uD83D\uDCA1 Best values:\n");
                msg.append("- Light: 70–100%\n- Humidity: 50–80%\n- Temperature: 18–30°C\n- Water: 200–800ml\n\n");
                msg.append("\uD83D\uDC4D Tips:\n");
                msg.append("- Place in a sunny, sheltered spot.\n");
                msg.append("- Feed with a balanced fertilizer.\n");
                msg.append("- Prune to encourage fruiting.\n\n");
                msg.append("\u26A0\uFE0F Avoid:\n");
                msg.append("- Overcrowding.\n- Waterlogging roots.\n");
                break;
            case "Trees":
                msg.append("\u2728 Trees\n");
                msg.append("Trees need space and steady care to thrive.\n\n");
                msg.append("\uD83D\uDCA1 Best values:\n");
                msg.append("- Light: 40–80%\n- Humidity: 30–60%\n- Temperature: 10–30°C\n- Water: 300–1000ml\n\n");
                msg.append("\uD83D\uDC4D Tips:\n");
                msg.append("- Water deeply, especially in dry periods.\n");
                msg.append("- Mulch around the base to retain moisture.\n");
                msg.append("- Prune dead branches regularly.\n\n");
                msg.append("\u26A0\uFE0F Avoid:\n");
                msg.append("- Planting too close to buildings.\n- Overwatering young trees.\n");
                break;
            case "Shrubs":
                msg.append("\u2728 Shrubs\n");
                msg.append("Shrubs are versatile and can be shaped for beauty or privacy.\n\n");
                msg.append("\uD83D\uDCA1 Best values:\n");
                msg.append("- Light: 40–80%\n- Humidity: 30–60%\n- Temperature: 10–30°C\n- Water: 200–800ml\n\n");
                msg.append("\uD83D\uDC4D Tips:\n");
                msg.append("- Prune to maintain shape and health.\n");
                msg.append("- Water at the base, not on the leaves.\n");
                msg.append("- Fertilize in early spring.\n\n");
                msg.append("\u26A0\uFE0F Avoid:\n");
                msg.append("- Over-pruning.\n- Letting soil stay soggy.\n");
                break;
            default:
                msg.append("No specific recommendations for this plant type.\n");
        }
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Recommendations for " + plantType);
        Label label = new Label(msg.toString());
        label.setStyle("-fx-font-size: 16px; -fx-padding: 24 24 12 24; -fx-text-fill: #444; -fx-font-family: 'Segoe UI', 'Arial', sans-serif;");
        label.setWrapText(true);
        ScrollPane scrollPane = new ScrollPane(label);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setPrefViewportHeight(380);
        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #ffd580; -fx-text-fill: #444; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 10 32; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, rgba(255,213,128,0.10), 4,0,0,1);");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #ffb347; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 10 32; -fx-font-size: 16px;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: #ffd580; -fx-text-fill: #444; -fx-font-weight: bold; -fx-background-radius: 18; -fx-padding: 10 32; -fx-font-size: 16px; -fx-effect: dropshadow(gaussian, rgba(255,213,128,0.10), 4,0,0,1);"));
        closeBtn.setOnAction(e -> dialog.close());
        VBox card = new VBox(scrollPane, closeBtn);
        card.setAlignment(Pos.CENTER);
        card.setSpacing(18);
        card.setStyle("-fx-background-color: #fffbe7; -fx-border-color: #ffe0b2; -fx-border-width: 2px; -fx-border-radius: 22; -fx-background-radius: 22; -fx-effect: dropshadow(gaussian, rgba(255,213,128,0.13), 10,0,0,2);");
        card.setPadding(new Insets(28, 28, 28, 28));
        VBox vbox = new VBox(card);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: linear-gradient(to bottom, #f9f6f2, #ffe0b2 80%);");
        Scene scene = new Scene(vbox, 540, 540);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void initializeWeatherDisplay() {
        try {
            List<WeatherService.WeatherData> forecast = WeatherService.getWeatherForecast();
            boolean rainNotified = false;
            Preferences prefs = Preferences.userNodeForPackage(getClass());
            String lastRainNotifDate = prefs.get("lastRainNotifDate", "");
            String today = LocalDate.now().toString();
            if (!forecast.isEmpty()) {
                VBox weatherContainer = new VBox(10);
                weatherContainer.setAlignment(Pos.CENTER);
                weatherContainer.setPadding(new Insets(15, 0, 15, 0));
                weatherContainer.setMaxWidth(1100);
                weatherContainer.setStyle("-fx-background-color: #f0f8f0; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,1); -fx-margin: 0 auto;");
                Label title = new Label("🌤️ Weather Forecast");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #14532d;");
                HBox forecastCards = new HBox(15);
                forecastCards.setAlignment(Pos.CENTER_LEFT);
                for (int i = 0; i < Math.min(3, forecast.size()); i++) {
                    WeatherService.WeatherData weather = forecast.get(i);
                    VBox dayCard = new VBox(8);
                    dayCard.setPadding(new Insets(12));
                    dayCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3,0,0,1);");
                    dayCard.setMinWidth(180);
                    Label dayLabel;
                    if (i == 0) {
                        todayTimeLabel = new Label();
                        updateTodayTimeLabel();
                        dayLabel = todayTimeLabel;
                        startLiveTimeUpdate();
                    } else {
                        dayLabel = new Label(weather.getDate());
                    }
                    dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    String weatherIcon = getWeatherIcon(weather.getDescription());
                    Label iconLabel = new Label(weatherIcon);
                    iconLabel.setStyle("-fx-font-size: 24px;");
                    Label tempLabel;
                    if (i == 0) {
                        tempLabel = new Label(String.format("Live: %.1f°C", weather.getCurrentTemp()));
                    } else {
                        tempLabel = new Label(String.format("%.1f°C / %.1f°C", weather.getMaxTemp(), weather.getMinTemp()));
                    }
                    tempLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    Label descLabel = new Label(weather.getDescription());
                    descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
                    if (weather.willRain()) {
                        Label rainWarning = new Label("☔ Skip watering your outdoor plants - Rain expected");
                        rainWarning.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12px;");
                        dayCard.getChildren().add(rainWarning);
                        if (!rainNotified && !today.equals(lastRainNotifDate)) {
                            sendTelegramMessage("☔ Rain expected soon! Skip watering your outdoor plants.");
                            prefs.put("lastRainNotifDate", today);
                            rainNotified = true;
                        }
                    }
                    dayCard.getChildren().addAll(dayLabel, iconLabel, tempLabel, descLabel);
                    forecastCards.getChildren().add(dayCard);
                }
                weatherContainer.getChildren().addAll(title, forecastCards);
                // Center weatherContainer in a full-width HBox
                HBox weatherBox = new HBox(weatherContainer);
                weatherBox.setAlignment(Pos.CENTER);
                weatherBox.setPadding(new Insets(0, 0, 20, 0));
                if (plantListContainer.getChildren().isEmpty() || !(plantListContainer.getChildren().get(0) instanceof HBox)) {
                    plantListContainer.getChildren().add(0, weatherBox);
                } else {
                    plantListContainer.getChildren().set(0, weatherBox);
                }
            }
        } catch (Exception e) {
            System.err.println("Error displaying weather: " + e.getMessage());
        }
    }
    
    private String getWeatherIcon(String description) {
        description = description.toLowerCase();
        if (description.contains("rain")) return "🌧️";
        if (description.contains("drizzle")) return "🌦️";
        if (description.contains("cloud")) return "⛅";
        if (description.contains("sun") || description.contains("clear")) return "☀️";
        if (description.contains("snow")) return "❄️";
        if (description.contains("thunder") || description.contains("storm")) return "⛈️";
        if (description.contains("fog") || description.contains("mist")) return "🌫️";
        return "🌤️";
    }

    private void updateTodayTimeLabel() {
        if (todayTimeLabel != null) {
            todayTimeLabel.setText("Today " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
    }

    private void startLiveTimeUpdate() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTodayTimeLabel()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void sendTelegramMessage(String message) {
        try {
            String urlString = String.format(
                "https://api.telegram.org/bot7632407003:AAFRqbO96DI5--ad-BiVQWrhCRhGOASZo44/sendMessage?chat_id=7744320596&text=%s",
                URLEncoder.encode(message, "UTF-8")
            );
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getInputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}