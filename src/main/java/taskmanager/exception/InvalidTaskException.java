package taskmanager.exception;

/**
 * Thrown when a task is invalid — for example, if the title is null/empty
 * or the due date is in the past.
 */
public class InvalidTaskException extends RuntimeException {
    /**
     * @param message description of why the task is invalid
     */
    public InvalidTaskException(String message) {
        super(message);
    }
}
