package taskmanager.impl;

import taskmanager.api.TaskService;
import taskmanager.exception.InvalidTaskException;
import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Task;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default reactive implementation of TaskService.
 * Wraps a shared CopyOnWriteArrayList with Mono/Flux reactive types.
 */
public class DefaultTaskService implements TaskService {

    private final CopyOnWriteArrayList<Task> tasks;

    /**
     * Constructs the service with a shared task list.
     *
     * @param tasks the shared thread-safe task list
     */
    public DefaultTaskService(CopyOnWriteArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * {@inheritDoc}
     *
     * @throws InvalidTaskException if task is null or has empty title/ID
     */
    @Override
    public Mono<Void> addTask(Task task) {
        return Mono.fromRunnable(() -> {
            if (task == null) throw new InvalidTaskException("Task must not be null");
            if (task.getTitle() == null || task.getTitle().isBlank())
                throw new InvalidTaskException("Task title must not be empty");
            tasks.add(task);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @throws TaskNotFoundException if no task with the given ID is found
     */
    @Override
    public Mono<Void> removeTask(String taskId) {
        return Mono.fromRunnable(() -> {
            boolean removed = tasks.removeIf(t -> t.getId().equals(taskId));
            if (!removed) throw new TaskNotFoundException(taskId);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @throws TaskNotFoundException if no task with the given ID is found
     */
    @Override
    public Mono<Task> findTaskById(String taskId) {
        return Flux.fromIterable(tasks)
                   .filter(t -> t.getId().equals(taskId))
                   .next()
                   .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Task> findAllTasks() {
        return Flux.fromIterable(tasks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<List<Task>> findAllTasksAsList() {
        return findAllTasks().collectList();
    }
}
