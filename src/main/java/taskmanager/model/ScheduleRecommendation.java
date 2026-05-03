package taskmanager.model;

/**
 * Represents a scheduling recommendation for a task.
 * Contains the task and a human-readable recommendation string.
 *
 * @param task           the task being recommended
 * @param recommendation a message describing the scheduling advice
 */
public record ScheduleRecommendation(Task task, String recommendation) {}
