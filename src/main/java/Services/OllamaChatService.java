package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.TimeUnit;

public class OllamaChatService {
    private static final Logger LOGGER = Logger.getLogger(OllamaChatService.class.getName());
    private static final String OLLAMA_API_URL = "http://localhost:11434/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String ask(String userPrompt) throws Exception {
        LOGGER.info("Envoi d'une requête au chatbot avec le prompt: " + userPrompt);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "mistral");
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        String jsonRequest = mapper.writeValueAsString(requestBody);
        LOGGER.fine("Requête JSON: " + jsonRequest);

        Request request = new Request.Builder()
            .url(OLLAMA_API_URL)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Pas de réponse";
                LOGGER.severe("Erreur de l'API Ollama: " + response.code() + " - " + errorBody);
                throw new RuntimeException("Erreur de l'API Ollama: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            LOGGER.fine("Réponse reçue: " + responseBody);
            
            JsonNode jsonResponse = mapper.readTree(responseBody);
            String content = jsonResponse
                .path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText()
                .trim();
                
            LOGGER.info("Réponse du chatbot: " + content);
            return content;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la communication avec Ollama", e);
            if (e instanceof java.net.SocketTimeoutException) {
                throw new RuntimeException("Le serveur Ollama met trop de temps à répondre. Veuillez vérifier que le service est bien en cours d'exécution et que le modèle Mistral est correctement chargé.", e);
            } else if (e instanceof java.net.ConnectException) {
                throw new RuntimeException("Impossible de se connecter au serveur Ollama. Veuillez vérifier que le service est bien démarré sur le port 11434.", e);
            } else {
                throw new RuntimeException("Erreur lors de la communication avec Ollama: " + e.getMessage(), e);
            }
        }
    }
} 