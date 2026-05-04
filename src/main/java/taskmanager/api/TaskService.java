package taskmanager.api;

import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.model.Task;

/**
 * Reactive service interface for task CRUD operations.
 * All methods return reactive types (Mono or Flux) and do NOT block.
 */
public interface TaskService {

    /**
     * Adds a task reactively.
     *
     * @param task the task to add
     * @return {@code Mono<Void>} that completes when the task is added,
     *         or errors with InvalidTaskException if task is invalid
     */
    Mono<Void> addTask(Task task);

    /**
     * Removes a task by ID reactively.
     *
     * @param taskId the ID of the task to remove
     * @return {@code Mono<Void>} that completes when removed,
     *         or errors with TaskNotFoundException if not found
     */
    Mono<Void> removeTask(String taskId);

    /**
     * Finds a single task by ID.
     *
     * @param taskId the ID to search for
     * @return {@code Mono<Task>} emitting the task, or errors with TaskNotFoundException
     */
    Mono<Task> findTaskById(String taskId);

    /**
     * Returns all tasks as a reactive stream.
     *
     * @return {@code Flux<Task>} emitting each task one by one
     */
    Flux<Task> findAllTasks();

    /**
     * Returns all tasks as a list inside a Mono.
     *
     * @return {@code Mono<List<Task>>} emitting the complete list of tasks
     */
    Mono<List<Task>> findAllTasksAsList();
}
