package taskmanager.exception;

/**
 * Thrown when the weather API call fails — for example, due to a network error,
 * invalid API key, or unexpected response format.
 */
public class WeatherAPIException extends RuntimeException {
    /**
 * Exception thrown when an error occurs while communicating with the weather service.
 * @param message description of the API failure
 *@param cause the underlying cause of the failure (e.g., IOException, JSONException)
 */
    public WeatherAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
