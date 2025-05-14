package Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject; // Ajoute cette importation en haut

public class EmailVerifier {

    private static final String API_KEY = "6714e9124d0dccb3b398f91d7bf07bcb9a71acbe";

    public static boolean verifyEmail(String email) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "https://api.hunter.io/v2/email-verifier?email=" + email + "&api_key=" + API_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Réponse API Hunter.io : " + response.body());

            // 👇 Parse JSON proprement
            JSONObject jsonResponse = new JSONObject(response.body());
            String status = jsonResponse.getJSONObject("data").getString("status");

            return "valid".equals(status);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}