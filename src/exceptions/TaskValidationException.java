package exceptions;

/**
 * NEW (sprint-8): бросается при пересечении задач по времени.
 */
public class TaskValidationException extends RuntimeException {
    public TaskValidationException(String message) { super(message); }
}
