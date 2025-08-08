package model;

import java.util.Objects;

/** Базовая задача. */
public class Task {
    protected String title;
    protected String description;
    protected int id;
    protected Status status;

    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    /** Тип задачи. */
    public TaskType getType() {
        return TaskType.TASK;
    }

    /* ========= геттеры/сеттеры ========= */

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /* ========= CSV-представление (для FileBackedTaskManager) ========= */
    public String toCsvRow() {
        // у обычной задачи поле epic пустое
        return String.join(",",
                String.valueOf(getId()),
                getType().name(),
                getTitle(),
                getStatus().name(),
                getDescription(),
                ""
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        // pattern matching TODO: —  убирает предупреждение «Variable 'task' can be replaced with pattern variable»
        if (!(o instanceof Task other)) {
            return false;
        }
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
}
