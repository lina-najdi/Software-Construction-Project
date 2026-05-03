package taskmanager.model;

import java.time.LocalDateTime;

/**
 * Holds weather forecast data for a specific location and time.
 * Used by SchedulePlanner to make weather-aware scheduling decisions.
 */
public class WeatherForecast {

    private final String location;
    private final LocalDateTime time;
    private final double temperatureCelsius;
    private final String condition;
    private final double precipitationProbability;

    /**
     * Constructs a WeatherForecast.
     *
     * @param location                city or location name
     * @param time                    time of the forecast
     * @param temperatureCelsius      temperature in Celsius
     * @param condition               weather condition description (e.g., "Clear", "Rain")
     * @param precipitationProbability probability of precipitation, value between 0.0 and 1.0
     */
    public WeatherForecast(String location, LocalDateTime time,
                           double temperatureCelsius,
                           String condition,
                           double precipitationProbability) {
        this.location = location;
        this.time = time;
        this.temperatureCelsius = temperatureCelsius;
        this.condition = condition;
        this.precipitationProbability = precipitationProbability;
    }

    /** @return the location for this forecast */
    public String getLocation() { return location; }

    /** @return the forecast time */
    public LocalDateTime getTime() { return time; }

    /** @return temperature in Celsius */
    public double getTemperatureCelsius() { return temperatureCelsius; }

    /** @return weather condition string */
    public String getCondition() { return condition; }

    /**
     * @return probability of precipitation as a value from 0.0 (no rain) to 1.0 (certain rain)
     */
    public double getPrecipitationProbability() { return precipitationProbability; }

    @Override
    public String toString() {
        return "WeatherForecast{location='" + location + "', condition='" + condition +
               "', temp=" + temperatureCelsius + "°C, rain=" + (precipitationProbability * 100) + "%}";
    }
}
