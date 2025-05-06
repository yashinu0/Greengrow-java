package Services;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class TranslationService {
    private static final String API_URL = "https://google-api31.p.rapidapi.com/translate";
    private static final String API_KEY = "a0f33b2339msh4c813d1f4c59b9ep16b983jsn4642c78b2651"; 
    private static final String API_HOST = "google-api31.p.rapidapi.com";
    private final OkHttpClient client = new OkHttpClient();

    public String translate(String text, String targetLanguage) {
        try {
            MediaType mediaType = MediaType.parse("application/json");
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("text", text);
            jsonBody.put("to", targetLanguage);
            jsonBody.put("from_lang", ""); // Laisser vide pour détection automatique

            RequestBody body = RequestBody.create(mediaType, jsonBody.toString());
            System.out.println("Request body: " + jsonBody.toString());

            Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("x-rapidapi-key", API_KEY)
                .addHeader("x-rapidapi-host", API_HOST)
                .addHeader("Content-Type", "application/json")
                .build();

            try (Response response = client.newCall(request).execute()) {
                // Log response headers
                System.out.println("Response headers:");
                response.headers().forEach(header -> 
                    System.out.println(header.getFirst() + ": " + header.getSecond())
                );

                String responseBody = response.body().string();
                System.out.println("Raw response body: " + responseBody);

                if (!response.isSuccessful()) {
                    System.err.println("Translation failed with status code: " + response.code());
                    System.err.println("Response body: " + responseBody);
                    return text;
                }

                try {
                    JSONArray jsonArray = new JSONArray(responseBody);
                    if (jsonArray.length() > 0) {
                        JSONObject firstTranslation = jsonArray.getJSONObject(0);
                        return firstTranslation.getString("translated");
                    } else {
                        System.err.println("Empty translation array");
                        return text;
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing response: " + e.getMessage());
                    System.err.println("Response body: " + responseBody);
                    return text;
                }
            }
        } catch (Exception e) {
            System.err.println("Error during translation: " + e.getMessage());
            e.printStackTrace();
            return text;
        }
    }
} 