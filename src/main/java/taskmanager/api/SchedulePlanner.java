package taskmanager.api;

import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;
import taskmanager.model.ScheduleRecommendation;
import reactor.core.publisher.Mono;
import java.util.List;

/**
 * Provides weather-aware scheduling recommendations for tasks.
 * Analyzes each task's weather sensitivity against current/forecast weather data.
 */
public interface SchedulePlanner {

    /**
     * Suggests a schedule for the given tasks based on the provided weather forecast.
     * Weather-sensitive tasks will receive warnings or "RISKY" status if the forecast
     * shows a high chance of rain or extreme temperatures.
     *
     * @param tasks    the list of tasks to evaluate
     * @param forecast the weather forecast to use for decision-making
     * @return Mono emitting a list of ScheduleRecommendation objects
     */
    Mono<List<ScheduleRecommendation>> suggestSchedule(List<Task> tasks, WeatherForecast forecast);

    /**
     * Suggests a schedule by first fetching the weather for the given location,
     * then evaluating each task against that forecast.
     *
     * @param tasks    the list of tasks to evaluate
     * @param location the city/location to fetch weather for (e.g., "Jeddah")
     * @return Mono emitting a list of ScheduleRecommendation objects,
     *         or errors with WeatherAPIException if the weather fetch fails
     */
    Mono<List<ScheduleRecommendation>> suggestScheduleForLocation(List<Task> tasks, String location);
}
