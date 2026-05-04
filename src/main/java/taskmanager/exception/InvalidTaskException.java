package taskmanager.exception;

/**
 * Thrown when a task is invalid — for example, if the title is null/empty
 * or the due date is in the past.
 */
public class InvalidTaskException extends RuntimeException {
    /**
 * Constructs a new InvalidTaskException with the specified detail message.
 *
 * @param message description of why the task is invalid
 */
    public InvalidTaskException(String message) {
        super(message);
    }
}
