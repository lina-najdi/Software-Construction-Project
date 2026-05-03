package taskmanager.api;

import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;
import taskmanager.impl.DefaultTaskManagerBuilder;

import reactor.core.publisher.Mono;
import java.util.List;

/**
 * Main facade for the Smart Task Manager.
 * This is the primary interface that other developers use to interact with the system.
 *
 * <p>Usage example:
 * <pre>
 *   TaskManager tm = TaskManager.builder()
 *       .withWeatherApiKey("API KEY")
 *       .build();
 *   tm.addTask(new Task("1", "Morning Run", LocalDateTime.now().plusHours(2), true));
 * </pre>
 */
public interface TaskManager {

    /**
     * Adds a task to the manager.
     *
     * @param task the task to add (must not be null, title must not be empty)
     * @throws taskmanager.exception.InvalidTaskException if task is null or has invalid fields
     */
    void addTask(Task task);

    /**
     * Removes a task by its ID.
     *
     * @param taskId the ID of the task to remove
     * @throws taskmanager.exception.TaskNotFoundException if no task with the given ID exists
     */
    void removeTask(String taskId);

    /**
     * Returns all currently stored tasks.
     *
     * @return list of all tasks (may be empty, never null)
     */
    List<Task> getTasks();

    /**
     * Fetches the current weather forecast for a given location asynchronously.
     * Does NOT block the calling thread.
     *
     * @param location the city name (e.g., "Jeddah")
     * @return a Mono that emits a WeatherForecast, or errors with WeatherAPIException
     */
    Mono<WeatherForecast> fetchWeather(String location);

    /**
     * Returns the SchedulePlanner associated with this TaskManager.
     *
     * @return the SchedulePlanner instance
     */
    taskmanager.api.SchedulePlanner getPlanner();

    /**
     * Creates a new builder for constructing a TaskManager instance.
     *
     * @return a fresh TaskManagerBuilder
     */
    static TaskManagerBuilder builder() {
        return new DefaultTaskManagerBuilder();
    }

    /**
     * Step-builder interface for constructing a TaskManager.
     */
    interface TaskManagerBuilder {

        /**
         * Sets the API key for the weather service.
         *
         * @param apiKey the OpenWeatherMap API key
         * @return this builder (for chaining)
         */
        TaskManagerBuilder withWeatherApiKey(String apiKey);

        /**
         * Sets an optional file path for persisting tasks.
         *
         * @param path file path to store tasks (optional)
         * @return this builder (for chaining)
         */
        TaskManagerBuilder withStoragePath(String path);

        /**
         * Builds and returns the configured TaskManager.
         *
         * @return a ready-to-use TaskManager instance
         */
        TaskManager build();
    }
}
