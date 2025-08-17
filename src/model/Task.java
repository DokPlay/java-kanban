package model;

import java.time.Duration;                // NEW (sprint-8)
import java.time.LocalDateTime;           // NEW (sprint-8)
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import util.CsvUtils;                     // sprint-9: используем общую утилиту для CSV-escape

/**
 * Базовая задача.
 * <p>CHANGED (sprint-8):
 * - добавлены поля duration и startTime;
 * - добавлен getEndTime();
 * - расширена CSV-строка (toCsvRow) на durationMinutes и startTime.
 */
@SuppressWarnings("DuplicatedCode")
public class Task {
    protected int id;
    protected String title;
    protected String description;
    protected Status status = Status.NEW;

    // NEW (sprint-8)
    protected Duration duration;           // оценка длительности (минуты)
    protected LocalDateTime startTime;     // когда начать

    // CSV-формат времени. Сохраняем человеко читаемо.
    public static final DateTimeFormatter CSV_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // NEW (sprint-8): удобный конструктор под тесты/инициализацию
    public Task(
            String title,
            String description,
            java.time.Duration duration,
            java.time.LocalDateTime startTime) {
        this(title, description);
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    /* ---------- геттеры/сеттеры ---------- */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // NEW (sprint-8)
    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    // NEW (sprint-8)
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    // NEW (sprint-8): вычисляем завершение как start + duration
    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    /* ---------- CSV ---------- */

    /**
     * Возвращает CSV-строку в формате:
     * id,type,name,status,description,durationMinutes,startTime,epic
     * Для Task поле epic — пустое.
     */
    public String toCsvRow() {
        String dur = duration == null ? "" : String.valueOf(duration.toMinutes());
        String st = startTime == null ? "" : startTime.format(CSV_TIME_FMT);
        return String.join(
                ",",
                String.valueOf(id),
                getType().name(),
                CsvUtils.escape(title),        // sprint-9: общий util вместо локального escape
                status.name(),
                CsvUtils.escape(description),  // sprint-9: общий util вместо локального escape
                dur,
                st,
                "" // epic
        );
    }

    /* ---------- equals/hashCode ---------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Task task)) {
            return false;
        }
        return id == task.id
                && Objects.equals(title, task.title)
                && Objects.equals(description, task.description)
                && status == task.status
                && Objects.equals(duration, task.duration)
                && Objects.equals(startTime, task.startTime)
                && getType() == task.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status, duration, startTime, getType());
    }

    @Override
    public String toString() {
        return getType()
                + "{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", status=" + status
                + ", duration=" + (duration == null ? "null" : duration.toMinutes() + "m")
                + ", startTime=" + startTime
                + '}';
    }
}
