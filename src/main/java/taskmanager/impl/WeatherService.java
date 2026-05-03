package taskmanager.impl;

import taskmanager.exception.WeatherAPIException;
import taskmanager.model.WeatherForecast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles HTTP calls to the OpenWeatherMap API.
 * Caches responses for 10 minutes per location to avoid unnecessary API calls.
 */
public class WeatherService {

    private final String apiKey;

    /** Cache: location → (forecast, fetchTimeMillis) */
    private final Map<String, CachedForecast> cache = new ConcurrentHashMap<>();

    /** Cache duration: 10 minutes */
    private static final long CACHE_DURATION_MS = 10 * 60 * 1000;

    /**
     * Constructs a WeatherService with the given API key.
     *
     * @param apiKey OpenWeatherMap API key
     */
    public WeatherService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Fetches weather synchronously for a given location.
     * Returns a cached result if available and not expired.
     * This method is intended to be called from a background thread.
     *
     * @param location city name (e.g., "Jeddah")
     * @return WeatherForecast for the location
     * @throws WeatherAPIException if the API call fails or returns an error
     */
    public WeatherForecast fetchWeatherSync(String location) {
        // Return cached if fresh
        CachedForecast cached = cache.get(location.toLowerCase());
        if (cached != null && System.currentTimeMillis() - cached.fetchedAt < CACHE_DURATION_MS) {
            return cached.forecast;
        }

        // If no API key, return a default forecast (for demo/testing)
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_API_KEY_HERE")) {
                return buildDemoForecast(location);
        }

        try {
            String urlStr = "https://api.openweathermap.org/data/2.5/weather?q="
                    + location.replace(" ", "+")
                    + "&appid=" + apiKey
                    + "&units=metric";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new WeatherAPIException("API returned HTTP " + responseCode + " for location: " + location, null);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            double temp = json.getJSONObject("main").getDouble("temp");
            String condition = json.getJSONArray("weather").getJSONObject(0).getString("main");

            // OpenWeatherMap doesn't directly give precipitation probability in the current endpoint
            // We infer it from the condition
            double precipProb = inferPrecipProbability(condition);

            WeatherForecast forecast = new WeatherForecast(
                    location, LocalDateTime.now(), temp, condition, precipProb
            );

            // Cache it
            cache.put(location.toLowerCase(), new CachedForecast(forecast, System.currentTimeMillis()));
            return forecast;

        } catch (WeatherAPIException e) {
            throw e;
        } catch (Exception e) {
            throw new WeatherAPIException("Failed to fetch weather for: " + location, e);
        }
    }

    /**
     * Infers precipitation probability from the weather condition string.
     *
     * @param condition weather condition from OpenWeatherMap
     * @return a probability between 0.0 and 1.0
     */
    private double inferPrecipProbability(String condition) {
        return switch (condition.toLowerCase()) {
            case "rain", "drizzle", "thunderstorm" -> 0.85;
            case "snow" -> 0.75;
            case "clouds" -> 0.30;
            case "mist", "fog", "haze" -> 0.20;
            default -> 0.05; // Clear, etc.
        };
    }

    /**
     * Returns a demo forecast when no API key is configured.
     * Used for testing and development.
     *
     * @param location the requested location
     * @return a simulated WeatherForecast
     */
    private WeatherForecast buildDemoForecast(String location) {
        return new WeatherForecast(location, LocalDateTime.now(), 32.0, "Clear", 0.1);
    }

    /** Internal cache entry */
    private record CachedForecast(WeatherForecast forecast, long fetchedAt) {}
}
