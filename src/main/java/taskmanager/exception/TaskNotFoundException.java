package taskmanager.exception;

/**
 * Thrown when a task with a given ID cannot be found in the task manager.
 */
public class TaskNotFoundException extends RuntimeException {
    /**
     * @param taskId the ID of the task that was not found
     */
    public TaskNotFoundException(String taskId) {
        super("Task not found: " + taskId);
    }
}
