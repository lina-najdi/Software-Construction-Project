package taskmanager.model;

import java.time.LocalDateTime;

/**
 * Represents a task in the Smart Task Manager.
 * A task has a unique ID, title, description, due date/time,
 * and a flag indicating if it is weather-sensitive.
 */
public class Task {

    private final String id;
    private String title;
    private String description;
    private LocalDateTime dueDateTime;
    private boolean weatherSensitive;

    /**
     * Constructs a new Task.
     *
     * @param id              unique identifier for the task (must not be null)
     * @param title           title of the task (must not be null or empty)
     * @param dueDateTime     when the task is due
     * @param weatherSensitive true if this task should be rescheduled based on weather
     */
    public Task(String id, String title, LocalDateTime dueDateTime, boolean weatherSensitive) {
        this.id = id;
        this.title = title;
        this.dueDateTime = dueDateTime;
        this.weatherSensitive = weatherSensitive;
    }

   /**
 * Gets the unique identifier for this task.
 * @return the unique ID of this task
 */
    public String getId() { return id; }

    /**
 * Gets the title of this task.
 * @return the title of this task
 */
    public String getTitle() { return title; }

    /**
     * Sets the title of this task.
     * @param title new title (must not be null or empty)
     */
    public void setTitle(String title) { this.title = title; }

   /**
 * Gets the detailed description of this task.
 * @return the description of this task, or null if not set
 */
    public String getDescription() { return description; }

    /**
     * Sets the description of this task.
     * @param description human-readable description
     */
    public void setDescription(String description) { this.description = description; }

    /**
 * Gets the date and time when this task is due.
 * @return the due date and time of this task
 */
    public LocalDateTime getDueDateTime() { return dueDateTime; }

    /**
     * Sets the due date and time.
     * @param dueDateTime new due date/time
     */
    public void setDueDateTime(LocalDateTime dueDateTime) { this.dueDateTime = dueDateTime; }

    /**
 * Checks if the task's completion depends on weather conditions.
 * @return true if this task is sensitive to weather conditions
 */
    public boolean isWeatherSensitive() { return weatherSensitive; }

    /**
     * Sets whether this task is weather-sensitive.
     * @param weatherSensitive true if the task depends on good weather
     */
    public void setWeatherSensitive(boolean weatherSensitive) { this.weatherSensitive = weatherSensitive; }

    /**
 * Returns a string representation of the task for debugging and logging.
 * @return a string containing task details
 */
    @Override
    public String toString() {
        return "Task{id='" + id + "', title='" + title + "', due=" + dueDateTime + ", weatherSensitive=" + weatherSensitive + "}";
    }
}
