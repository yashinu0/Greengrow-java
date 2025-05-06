package Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.time.LocalDateTime;

public class WeatherService {
    private static final String API_KEY = "157bca46bd79bed9a7cbccd94a725212";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5";
    private static final double LAT = 36.90218572346004;
    private static final double LON = 10.189320433390641;

    public static class WeatherData {
        private final String date;
        private final double minTemp;
        private final double maxTemp;
        private final double currentTemp;
        private final String description;
        private final boolean willRain;

        public WeatherData(String date, double minTemp, double maxTemp, double currentTemp, String description, boolean willRain) {
            this.date = date;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.currentTemp = currentTemp;
            this.description = description;
            this.willRain = willRain;
        }

        public String getDate() { return date; }
        public double getMinTemp() { return minTemp; }
        public double getMaxTemp() { return maxTemp; }
        public double getCurrentTemp() { return currentTemp; }
        public String getDescription() { return description; }
        public boolean willRain() { return willRain; }
    }

    private static String formatDate(LocalDate date) {
        if (date.equals(LocalDate.now())) {
            return "Today";
        } else if (date.equals(LocalDate.now().plusDays(1))) {
            return "Tomorrow";
        } else {
            return date.format(DateTimeFormatter.ofPattern("EEEE"));
        }
    }

    private static double parseTemperature(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return 0.0;
        }
        try {
            return element.getAsDouble();
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static String parseDescription(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return "Unknown";
        }
        return element.getAsString();
    }

    private static JsonObject getListItem(JsonArray array, int index) {
        return array.get(index).getAsJsonObject();
    }

    private static JsonObject getWeatherData(JsonArray array, int index) {
        JsonObject data = getListItem(array, index);
        JsonObject mainData = data.getAsJsonObject("main");
        JsonArray weatherArray = data.getAsJsonArray("weather");
        JsonObject weatherData = weatherArray.get(0).getAsJsonObject();
        
        double temp = parseTemperature(mainData.get("temp"));
        String desc = parseDescription(weatherData.get("description"));
        
        JsonObject result = new JsonObject();
        result.addProperty("temperature", temp);
        result.addProperty("description", desc);
        
        return result;
    }

    public static List<WeatherData> getWeatherForecast() {
        List<WeatherData> forecast = new ArrayList<>();
        try {
            // Get current weather
            String currentUrl = String.format("%s/weather?lat=%.6f&lon=%.6f&appid=%s&units=metric", 
                BASE_URL, LAT, LON, API_KEY);
            JsonObject currentWeather = getJsonResponse(currentUrl);
            
            // Get forecast
            String forecastUrl = String.format("%s/forecast?lat=%.6f&lon=%.6f&appid=%s&units=metric", 
                BASE_URL, LAT, LON, API_KEY);
            JsonObject forecastData = getJsonResponse(forecastUrl);
            JsonArray list = forecastData.getAsJsonArray("list");

            // Process current weather
            double currentTemp = currentWeather.getAsJsonObject("main").get("temp").getAsDouble();
            String currentDesc = currentWeather.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString();
            boolean currentRain = currentDesc.toLowerCase().contains("rain");
            
            forecast.add(new WeatherData(
                formatDate(LocalDate.now()),
                0, 0, currentTemp,
                currentDesc,
                currentRain
            ));

            // Process next 2 days
            for (int i = 1; i <= 2; i++) {
                LocalDate targetDate = LocalDate.now().plusDays(i);
                double minTemp = Double.MAX_VALUE;
                double maxTemp = Double.MIN_VALUE;
                String description = "";
                boolean willRain = false;

                for (int j = 0; j < list.size(); j++) {
                    JsonObject item = list.get(j).getAsJsonObject();
                    LocalDate itemDate = LocalDateTime.parse(
                        item.get("dt_txt").getAsString(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    ).toLocalDate();

                    if (itemDate.equals(targetDate)) {
                        double temp = item.getAsJsonObject("main").get("temp").getAsDouble();
                        minTemp = Math.min(minTemp, temp);
                        maxTemp = Math.max(maxTemp, temp);
                        
                        String desc = item.getAsJsonArray("weather").get(0).getAsJsonObject()
                            .get("description").getAsString();
                        if (!description.contains("rain") && desc.toLowerCase().contains("rain")) {
                            description = desc;
                            willRain = true;
                        } else if (description.isEmpty()) {
                            description = desc;
                        }
                    }
                }

                forecast.add(new WeatherData(
                    formatDate(targetDate),
                    minTemp, maxTemp, 0,
                    description,
                    willRain
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return forecast;
    }

    private static JsonObject getJsonResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return JsonParser.parseString(response.toString()).getAsJsonObject();
    }
} 