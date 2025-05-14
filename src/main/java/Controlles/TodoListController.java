package Controlles;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.prefs.Preferences;
import org.json.JSONObject;

public class TodoListController {
    @FXML
    private VBox todoListContainer;
    
    private static final String TODOIST_API_TOKEN = "d9e93263e36daa961addeec03ca069d27762c596"; // Replace with your token
    private static final String TODOIST_API_URL = "https://api.todoist.com/rest/v2/tasks";
    
    public void initialize() {
        // Initialize the todo list
    }
    
    public void addTodoItem(String plantName, String message) {
        HBox todoItem = new HBox(10);
        todoItem.setAlignment(Pos.CENTER_LEFT);
        todoItem.setPadding(new Insets(10));
        todoItem.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        
        Label plantLabel = new Label("🌱 " + plantName);
        plantLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #666;");
        messageLabel.setWrapText(true);
        
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        deleteButton.setOnAction(e -> {
            todoListContainer.getChildren().remove(todoItem);
            deleteTodoistTask(plantName);
        });
        
        todoItem.getChildren().addAll(plantLabel, messageLabel, deleteButton);
        todoListContainer.getChildren().add(todoItem);
        
        // Create Todoist task
        createTodoistTask(plantName, message);
    }
    
    private void createTodoistTask(String plantName, String message) {
        try {
            JSONObject json = new JSONObject();
            json.put("content", plantName + ": " + message);
            
            URL url = new URL(TODOIST_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + TODOIST_API_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            conn.getOutputStream().write(json.toString().getBytes());
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Todoist task created successfully");
            } else {
                System.out.println("Failed to create Todoist task. Response code: " + responseCode);
            }
            
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void deleteTodoistTask(String plantName) {
        // Note: This is a simplified version. In a real implementation,
        // you would need to store the task ID when creating the task
        // and use it to delete the specific task
        System.out.println("Todoist task deletion would be implemented here");
    }
} 