package taskmanager.impl;

import taskmanager.api.TaskManager;
import taskmanager.api.SchedulePlanner;
import taskmanager.exception.InvalidTaskException;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.exception.WeatherAPIException;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of the TaskManager interface.
 * Stores tasks in memory and fetches weather data from OpenWeatherMap.
 *
 * <p>Thread-safe: uses CopyOnWriteArrayList for concurrent access.</p>
 */
public class DefaultTaskManager implements TaskManager {

    /** Thread-safe list of tasks */
    private final CopyOnWriteArrayList<Task> tasks = new CopyOnWriteArrayList<>();

    private final String weatherApiKey;
    private final WeatherService weatherService;
    private final SchedulePlanner planner;

    /**
     * Package-private constructor — use TaskManager.builder() to create an instance.
     *
     * @param weatherApiKey API key for OpenWeatherMap
     */
    DefaultTaskManager(String weatherApiKey) {
        this.weatherApiKey = weatherApiKey;
        this.weatherService = new WeatherService(weatherApiKey);
        this.planner = new DefaultSchedulePlanner(weatherService);
    }

    /**
     * {@inheritDoc}
     *
     * @throws InvalidTaskException if task is null, has null/empty title, or null ID
     */
    @Override
    public void addTask(Task task) {
        if (task == null) throw new InvalidTaskException("Task must not be null");
        if (task.getId() == null || task.getId().isBlank())
            throw new InvalidTaskException("Task ID must not be null or empty");
        if (task.getTitle() == null || task.getTitle().isBlank())
            throw new InvalidTaskException("Task title must not be null or empty");

        tasks.add(task);
    }

    /**
     * {@inheritDoc}
     *
     * @throws TaskNotFoundException if no task with the given ID exists
     */
    @Override
    public void removeTask(String taskId) {
        boolean removed = tasks.removeIf(t -> t.getId().equals(taskId));
        if (!removed) throw new TaskNotFoundException(taskId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Task> getTasks() {
        return Collections.unmodifiableList(new ArrayList<>(tasks));
    }

    /**
     * {@inheritDoc}
     * Runs the HTTP call on a background thread (boundedElastic scheduler).
     *
     * @throws WeatherAPIException if the API call fails
     */
    @Override
    public Mono<WeatherForecast> fetchWeather(String location) {
        return Mono.fromCallable(() -> weatherService.fetchWeatherSync(location))
                   .subscribeOn(Schedulers.boundedElastic())
                   .onErrorMap(e -> !(e instanceof WeatherAPIException),
                               e -> new WeatherAPIException("Failed to fetch weather for: " + location, e));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SchedulePlanner getPlanner() {
        return planner;
    }

    /**
     * Returns the internal task service view for reactive operations.
     *
     * @return a DefaultTaskService backed by this manager's task list
     */
    public DefaultTaskService getTaskService() {
        return new DefaultTaskService(tasks);
    }
}
