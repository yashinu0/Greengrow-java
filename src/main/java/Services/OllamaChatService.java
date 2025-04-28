package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.util.*;

public class OllamaChatService {
    private static final String OLLAMA_API_URL = "http://localhost:11434/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String ask(String userPrompt) throws Exception {
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

        Request request = new Request.Builder()
            .url(OLLAMA_API_URL)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "Erreur du chatbot : " + response.body().string();
            }
            JsonNode jsonResponse = mapper.readTree(response.body().string());
            return jsonResponse
                .path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText()
                .trim();
        }
    }
} 