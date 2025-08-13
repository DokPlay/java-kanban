package model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Подзадача, привязанная к эпику.
 *
 * <p>CHANGED (sprint-8): унаследованы duration/startTime/getEndTime от Task.
 */
public class Subtask extends Task {

    private int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, Status status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @SuppressWarnings("unused")
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toCsvRow() {
        String dur = duration == null ? "" : String.valueOf(duration.toMinutes());
        String st = startTime == null ? "" : startTime.format(CSV_TIME_FMT);
        return String.join(
                ",",
                String.valueOf(id),
                getType().name(),
                escape(title),
                status.name(),
                escape(description),
                dur,
                st,
                String.valueOf(epicId)
        );
    }

    private static String escape(String s) {
        return s == null ? "" : s;
    }

    // Удобные fluent-сеттеры TODO:(по желанию)
    @SuppressWarnings("unused")
    public Subtask withStart(LocalDateTime start) {
        this.startTime = start;
        return this;
    }

    @SuppressWarnings("unused")
    public Subtask withDuration(Duration d) {
        this.duration = d;
        return this;
    }
}
