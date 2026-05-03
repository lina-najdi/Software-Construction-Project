package taskmanager.impl;

import taskmanager.api.SchedulePlanner;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of SchedulePlanner.
 * Evaluates each task against weather data and generates recommendations.
 *
 * <p>Rules:
 * <ul>
 *   <li>Weather-sensitive task + rain probability > 60% → RISKY </li>
 *   <li>Weather-sensitive task + temperature > 45°C → RISKY (extreme heat) </li>
 *   <li>Weather-sensitive task + good weather → SAFE </li>
 *   <li>Non-weather-sensitive task → No weather impact </li>
 * </ul>
 */
public class DefaultSchedulePlanner implements SchedulePlanner {

    private final WeatherService weatherService;

    /**
     * @param weatherService the service used to fetch weather data
     */
    public DefaultSchedulePlanner(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestSchedule(List<Task> tasks, WeatherForecast forecast) {
        return Mono.fromCallable(() ->
            tasks.stream()
                 .map(task -> new ScheduleRecommendation(task, buildRecommendation(task, forecast)))
                 .collect(Collectors.toList())
        ).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(List<Task> tasks, String location) {
        return Mono.fromCallable(() -> weatherService.fetchWeatherSync(location))
                   .subscribeOn(Schedulers.boundedElastic())
                   .flatMap(forecast -> suggestSchedule(tasks, forecast));
    }

    /**
     * Builds a recommendation string for a single task based on the forecast.
     *
     * @param task     the task to evaluate
     * @param forecast the current weather forecast
     * @return a human-readable recommendation string
     */
    private String buildRecommendation(Task task, WeatherForecast forecast) {
        if (!task.isWeatherSensitive()) {
            return "No weather impact — proceed as planned.";
        }

        double rain = forecast.getPrecipitationProbability();
        double temp = forecast.getTemperatureCelsius();

        if (rain > 0.6) {
            return String.format("RISKY — %.0f%% chance of rain. Consider rescheduling this outdoor task.", rain * 100);
        }
        if (temp > 45) {
            return String.format("RISKY — Extreme heat (%.1f°C). Avoid outdoor activities.", temp);
        }
        if (rain > 0.3) {
            return String.format("CAUTION — %.0f%% chance of rain. Keep an eye on the weather.", rain * 100);
        }

        return String.format("SAFE — Good weather (%.1f°C, %s). Proceed as planned.", temp, forecast.getCondition());
    }
}
