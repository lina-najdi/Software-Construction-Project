package taskmanager.exception;

/**
 * Thrown when the weather API call fails — for example, due to a network error,
 * invalid API key, or unexpected response format.
 */
public class WeatherAPIException extends RuntimeException {
    /**
     * @param message description of the API failure
     * @param cause   the underlying cause (e.g., IOException)
     */
    public WeatherAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
