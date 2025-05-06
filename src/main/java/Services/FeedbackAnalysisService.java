package Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class FeedbackAnalysisService {
    private static final String OLLAMA_API_URL = "http://localhost:11434/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public enum SentimentType {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }

    public SentimentType analyzeFeedbackSentiment(String feedbackText) {
        try {
            String prompt = String.format(
                "Classify the sentiment of this feedback as POSITIVE, NEGATIVE, or NEUTRAL. Respond with only one word (POSITIVE, NEGATIVE, or NEUTRAL). Feedback: %s",
                feedbackText
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "mistral");
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);

            String jsonRequest = mapper.writeValueAsString(requestBody);

            Request request = new Request.Builder()
                .url(OLLAMA_API_URL)
                .addHeader("Content-Type", "application/json")
                // Pas besoin d'Authorization pour Ollama
                .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("Error from Ollama API: " + response.body().string());
                    return SentimentType.NEUTRAL; // Default to neutral in case of error
                }

                JsonNode jsonResponse = mapper.readTree(response.body().string());
                String sentiment = jsonResponse
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim()
                    .toUpperCase();

                // Nettoyage possible (parfois le modèle répond "POSITIVE." ou "POSITIVE\n")
                sentiment = sentiment.replaceAll("[^A-Z]", "");

                // Sécurité : si la réponse n'est pas reconnue, retourne NEUTRAL
                if (!sentiment.equals("POSITIVE") && !sentiment.equals("NEGATIVE") && !sentiment.equals("NEUTRAL")) {
                    return SentimentType.NEUTRAL;
                }

                return SentimentType.valueOf(sentiment);
            }
        } catch (Exception e) {
            System.err.println("Error analyzing feedback: " + e.getMessage());
            return SentimentType.NEUTRAL; // Default to neutral in case of error
        }
    }
}