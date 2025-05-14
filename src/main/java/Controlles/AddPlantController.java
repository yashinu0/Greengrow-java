package Controlles;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import Entities.Alerte;
import Services.AlerteService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

public class AddPlantController implements Initializable {

    @FXML private TextField plantNameField;
    @FXML private ComboBox<String> plantTypeCombo;
    @FXML private RadioButton indoorRadio;
    @FXML private RadioButton outdoorRadio;
    @FXML private ToggleGroup locationGroup;
    @FXML private TextField lightField;
    @FXML private TextField humidityField;
    @FXML private TextField temperatureField;
    @FXML private TextField waterField;
    @FXML private Text nameError;
    @FXML private Text typeError;
    @FXML private Text lightError;
    @FXML private Text humidityError;
    @FXML private Text tempError;
    @FXML private Text waterError;

    // Plant type needs (same as in RenduController)
    private static class PlantNeeds {
        int minLight, maxLight, minHum, maxHum, minTemp, maxTemp, minWater, maxWater;
        PlantNeeds(int minL, int maxL, int minH, int maxH, int minT, int maxT, int minW, int maxW) {
            minLight = minL; maxLight = maxL; minHum = minH; maxHum = maxH; minTemp = minT; maxTemp = maxT; minWater = minW; maxWater = maxW;
        }
    }
    private static final java.util.Map<String, PlantNeeds> PLANT_TYPE_NEEDS = new java.util.HashMap<>();
    static {
        // Succulents: Small potted succulents like Echeveria, Haworthia
        PLANT_TYPE_NEEDS.put("Succulents", new PlantNeeds(50, 80, 20, 40, 18, 28, 30, 100));
        
        // Flowering Plants: Small flowering plants like African Violet, Orchid
        PLANT_TYPE_NEEDS.put("Flowering Plants", new PlantNeeds(40, 70, 40, 60, 18, 25, 50, 150));
        
        // Herbs: Small potted herbs like Basil, Mint, Rosemary
        PLANT_TYPE_NEEDS.put("Herbs", new PlantNeeds(40, 70, 40, 60, 15, 25, 40, 120));
        
        // Vegetables: Small potted vegetables like Cherry Tomato, Lettuce
        PLANT_TYPE_NEEDS.put("Vegetables", new PlantNeeds(50, 80, 40, 60, 16, 25, 50, 150));
        
        // Fruits: Small potted fruits like Dwarf Strawberry, Dwarf Blueberry
        PLANT_TYPE_NEEDS.put("Fruits", new PlantNeeds(50, 80, 40, 60, 18, 25, 50, 150));
        
        // Trees: Small potted trees like Ficus, Money Tree
        PLANT_TYPE_NEEDS.put("Trees", new PlantNeeds(30, 60, 30, 50, 15, 25, 50, 150));
        
        // Shrubs: Small potted shrubs like Azalea, Gardenia
        PLANT_TYPE_NEEDS.put("Shrubs", new PlantNeeds(30, 60, 40, 60, 15, 25, 50, 150));
    }

    private TodoListController todoListController;

    public void setTodoListController(TodoListController controller) {
        this.todoListController = controller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPlantTypes();
        setupFormValidation();
        // Set up location toggle group
        locationGroup = new ToggleGroup();
        indoorRadio.setToggleGroup(locationGroup);
        outdoorRadio.setToggleGroup(locationGroup);
        // No default selection
        locationGroup.selectToggle(null);
    }

    private void setupPlantTypes() {
        plantTypeCombo.getItems().addAll(
                "Flowering Plants", "Succulents", "Herbs",
                "Vegetables", "Fruits", "Trees", "Shrubs"
        );
    }

    private void setupFormValidation() {
        StringConverter<Integer> converter = new IntegerStringConverter();

        lightField.setTextFormatter(createNumberTextFormatter(converter));
        humidityField.setTextFormatter(createNumberTextFormatter(converter));
        temperatureField.setTextFormatter(createNumberTextFormatter(converter));
        waterField.setTextFormatter(createNumberTextFormatter(converter));
    }

    private TextFormatter<Integer> createNumberTextFormatter(StringConverter<Integer> converter) {
        return new TextFormatter<>(converter, 0, change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        });
    }

    @FXML
    private void handleAddPlant() {
        if (validateForm()) {
            // Save plant to database
            String plantName = plantNameField.getText();
            String plantType = plantTypeCombo.getValue();
            java.time.LocalDate dateAdded = java.time.LocalDate.now();

            Entities.Rendu rendu = new Entities.Rendu(plantName, plantType, dateAdded);
            Services.RenduService renduService = new Services.RenduService();
            try {
                renduService.addRendu(rendu);
                // Save sensor values and location to Preferences
                int lum = Integer.parseInt(lightField.getText());
                int hum = Integer.parseInt(humidityField.getText());
                int temp = Integer.parseInt(temperatureField.getText());
                int water = Integer.parseInt(waterField.getText());
                String location = "N/A";
                if (locationGroup.getSelectedToggle() != null) {
                    location = ((javafx.scene.control.RadioButton) locationGroup.getSelectedToggle()).getText();
                }
                String key = "plant_" + plantName + "_" + plantType + "_" + dateAdded;
                String value = lum + "," + hum + "," + temp + "," + water + "," + location;
                Preferences prefs = Preferences.userNodeForPackage(getClass());
                prefs.put(key, value);

                // Generate and save alert
                PlantNeeds needs = PLANT_TYPE_NEEDS.getOrDefault(plantType, new PlantNeeds(0,100,0,100,0,100,0,10000));
                StringBuilder recommendation = new StringBuilder();
                StringBuilder notifMsg = new StringBuilder();
                String urgency = "safe";
                notifMsg.append("⚠️ Plant in critical condition: ").append(plantName).append(" (").append(plantType).append(")\n");
                if (lum < needs.minLight) {
                    recommendation.append("Light is too low, move the plant to a brighter spot. ");
                    notifMsg.append("• Light is too low. Move to a brighter spot.\n");
                    urgency = "critical condition";
                } else if (lum > needs.maxLight) {
                    recommendation.append("Light is too high, move the plant to a less bright spot. ");
                    notifMsg.append("• Light is too high. Move to a less bright spot.\n");
                    urgency = "critical condition";
                }
                if (hum < needs.minHum) {
                    recommendation.append("Humidity is too low, increase humidity. ");
                    notifMsg.append("• Humidity is too low. Increase humidity (mist or humidifier).\n");
                    urgency = "critical condition";
                } else if (hum > needs.maxHum) {
                    recommendation.append("Humidity is too high, reduce humidity. ");
                    notifMsg.append("• Humidity is too high. Reduce humidity (ventilate or dehumidifier).\n");
                    urgency = "critical condition";
                }
                if (temp < needs.minTemp) {
                    recommendation.append("Temperature is too low, keep the plant warmer. ");
                    notifMsg.append("• Temperature is too low. Move to a warmer spot.\n");
                    urgency = "critical condition";
                } else if (temp > needs.maxTemp) {
                    recommendation.append("Temperature is too high, keep the plant cooler. ");
                    notifMsg.append("• Temperature is too high. Move to a cooler spot.\n");
                    urgency = "critical condition";
                }
                if (water < needs.minWater) {
                    recommendation.append("Water is too low, water the plant more often. ");
                    notifMsg.append("• Water is too low. Water more often.\n");
                    urgency = "critical condition";
                } else if (water > needs.maxWater) {
                    recommendation.append("Water is too high, water the plant less often. ");
                    notifMsg.append("• Water is too high. Water less often.\n");
                    urgency = "critical condition";
                }
                if (urgency.equals("safe")) {
                    recommendation.append("All values are optimal. Keep up the good work! ");
                }
                Alerte alerte = new Alerte(urgency, LocalDateTime.now());
                alerte.setRendu(rendu); // Link the plant to the alert
                // Optionally, you can add a recommendation field to Alerte if you want to display it
                // Save the alert
                AlerteService alerteService = new AlerteService();
                alerteService.addAlerte(alerte);

                // Show success message
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Plant added successfully!\n" + recommendation.toString());
                alert.showAndWait();

                if (urgency.equals("critical condition")) {
                    sendTelegramMessage(notifMsg.toString());
                    
                    // Create todo item for critical plant
                    if (todoListController != null) {
                        todoListController.addTodoItem(plantName, notifMsg.toString());
                    }
                }
            } catch (Exception e) {
                // Optionally show an error dialog
                e.printStackTrace();
            }

            // Close the form
            ((Stage) plantNameField.getScene().getWindow()).close();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) plantNameField.getScene().getWindow()).close();
    }

    @FXML
    private void handleGenerateRandomData() {
        Random rand = new Random();
        
        // First select a random plant type
        if (!plantTypeCombo.getItems().isEmpty()) {
            plantTypeCombo.getSelectionModel().select(rand.nextInt(plantTypeCombo.getItems().size()));
        }
        String plantType = plantTypeCombo.getValue();
        
        // Generate appropriate name based on plant type
        String[] names;
        switch (plantType) {
            case "Succulents":
                names = new String[]{"Aloe Vera", "Echeveria", "Haworthia", "Jade Plant", "Snake Plant", "Zebra Cactus"};
                break;
            case "Flowering Plants":
                names = new String[]{"Orchid", "Peace Lily", "African Violet", "Begonia", "Geranium", "Kalanchoe"};
                break;
            case "Herbs":
                names = new String[]{"Basil", "Mint", "Rosemary", "Thyme", "Parsley", "Oregano"};
                break;
            case "Vegetables":
                names = new String[]{"Tomato", "Bell Pepper", "Lettuce", "Spinach", "Carrot", "Radish"};
                break;
            case "Fruits":
                names = new String[]{"Strawberry", "Blueberry", "Lemon", "Lime", "Dwarf Orange", "Dwarf Apple"};
                break;
            case "Trees":
                names = new String[]{"Ficus", "Money Tree", "Dracaena", "Rubber Plant", "Norfolk Pine", "Umbrella Tree"};
                break;
            case "Shrubs":
                names = new String[]{"Boxwood", "Azalea", "Hibiscus", "Gardenia", "Camellia", "Hydrangea"};
                break;
            default:
                names = new String[]{"Plant"};
        }
        
        plantNameField.setText(names[rand.nextInt(names.length)] + " " + (rand.nextInt(100) + 1));
        
        // Random location
        if (rand.nextBoolean()) {
            indoorRadio.setSelected(true);
        } else {
            outdoorRadio.setSelected(true);
        }

        // Ask user for safe or critical
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Safe", "Safe", "Critical");
        dialog.setTitle("Generate Random Data");
        dialog.setHeaderText("Choose the type of data to generate:");
        dialog.setContentText("Type:");
        java.util.Optional<String> result = dialog.showAndWait();
        String selectedType = result.orElse("Safe");
        
        // Get plant type needs
        PlantNeeds needs = PLANT_TYPE_NEEDS.getOrDefault(plantType, new PlantNeeds(0,100,0,100,0,100,0,10000));
        
        if ("Safe".equals(selectedType)) {
            // Generate safe values within the optimal range
            lightField.setText(String.valueOf(rand.nextInt(needs.maxLight - needs.minLight + 1) + needs.minLight));
            humidityField.setText(String.valueOf(rand.nextInt(needs.maxHum - needs.minHum + 1) + needs.minHum));
            temperatureField.setText(String.valueOf(rand.nextInt(needs.maxTemp - needs.minTemp + 1) + needs.minTemp));
            waterField.setText(String.valueOf(rand.nextInt(needs.maxWater - needs.minWater + 1) + needs.minWater));
        } else {
            // For critical data, randomly select 1-3 problems
            int numProblems = rand.nextInt(3) + 1; // 1 to 3 problems
            boolean[] hasProblem = new boolean[4]; // Track which sensors have problems
            
            // Initialize all values within normal range first
            int[] values = new int[4];
            values[0] = rand.nextInt(needs.maxLight - needs.minLight + 1) + needs.minLight;
            values[1] = rand.nextInt(needs.maxHum - needs.minHum + 1) + needs.minHum;
            values[2] = rand.nextInt(needs.maxTemp - needs.minTemp + 1) + needs.minTemp;
            values[3] = rand.nextInt(needs.maxWater - needs.minWater + 1) + needs.minWater;
            
            // Randomly select which sensors will have problems
            for (int i = 0; i < numProblems; i++) {
                int which;
                do {
                    which = rand.nextInt(4);
                } while (hasProblem[which]);
                hasProblem[which] = true;
                
                // Set realistic out-of-range values
            switch (which) {
                case 0: // light
                        if (rand.nextBoolean()) {
                            // Too low: 0-30% of minimum
                            values[0] = (int)(needs.minLight * (rand.nextDouble() * 0.3));
                        } else {
                            // Too high: 80-100% of maximum
                            values[0] = needs.maxLight + (int)((100 - needs.maxLight) * (rand.nextDouble() * 0.2 + 0.8));
                            if (values[0] > 100) values[0] = 100;
                        }
                    break;
                    case 1: // humidity
                        if (rand.nextBoolean()) {
                            // Too low: 0-30% of minimum
                            values[1] = (int)(needs.minHum * (rand.nextDouble() * 0.3));
                        } else {
                            // Too high: 80-100% of maximum
                            values[1] = needs.maxHum + (int)((100 - needs.maxHum) * (rand.nextDouble() * 0.2 + 0.8));
                            if (values[1] > 100) values[1] = 100;
                        }
                    break;
                    case 2: // temperature
                        if (rand.nextBoolean()) {
                            // Too low: 5-10°C below minimum
                            values[2] = needs.minTemp - (rand.nextInt(5) + 5);
                        } else {
                            // Too high: 5-10°C above maximum
                            values[2] = needs.maxTemp + (rand.nextInt(5) + 5);
                        }
                    break;
                case 3: // water
                        if (rand.nextBoolean()) {
                            // Too low: 0-30% of minimum
                            values[3] = (int)(needs.minWater * (rand.nextDouble() * 0.3));
                        } else {
                            // Too high: 150-200% of maximum
                            values[3] = needs.maxWater + (int)(needs.maxWater * (rand.nextDouble() * 0.5 + 0.5));
                        }
                    break;
                }
            }
            
            lightField.setText(String.valueOf(values[0]));
            humidityField.setText(String.valueOf(values[1]));
            temperatureField.setText(String.valueOf(values[2]));
            waterField.setText(String.valueOf(values[3]));
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Name validation
        if (plantNameField.getText().isEmpty()) {
            nameError.setText("Name is required");
            nameError.setFill(Color.RED);
            isValid = false;
        } else {
            nameError.setText("");
        }

        // Type validation
        if (plantTypeCombo.getValue() == null) {
            typeError.setText("Type is required");
            typeError.setFill(Color.RED);
            isValid = false;
        } else {
            typeError.setText("");
        }

        // Light validation
        if (lightField.getText().isEmpty()) {
            lightError.setText("Light is required");
            lightError.setFill(Color.RED);
            isValid = false;
        } else {
            lightError.setText("");
        }

        // Humidity validation
        if (humidityField.getText().isEmpty()) {
            humidityError.setText("Humidity is required");
            humidityError.setFill(Color.RED);
            isValid = false;
        } else {
            int humidity = Integer.parseInt(humidityField.getText());
            if (humidity < 0 || humidity > 100) {
                humidityError.setText("Must be 0-100");
                humidityError.setFill(Color.RED);
                isValid = false;
            } else {
                humidityError.setText("");
            }
        }

        // Temperature validation
        if (temperatureField.getText().isEmpty()) {
            tempError.setText("Temperature is required");
            tempError.setFill(Color.RED);
            isValid = false;
        } else {
            tempError.setText("");
        }

        // Water validation
        if (waterField.getText().isEmpty()) {
            waterError.setText("Water is required");
            waterError.setFill(Color.RED);
            isValid = false;
        } else {
            waterError.setText("");
        }

        return isValid;
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