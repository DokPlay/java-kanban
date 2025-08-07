package model;

import java.util.Objects;

/** Базовая задача. */
public class Task {
    /* поля -------------------------------------------------------- */
    protected String title;
    protected String description;
    protected int     id;
    protected Status  status;

    /* конструктор ------------------------------------------------- */
    public Task(String title, String description, Status status) {
        this.title       = title;
        this.description = description;
        this.status      = status;
    }

    /* тип задачи -------------------------------------------------- */
    public TaskType getType() {       // нужен сериализации
        return TaskType.TASK;
    }

    /* геттеры / сеттеры ------------------------------------------ */
    public int     getId()          { return id; }
    public void    setId(int id)    { this.id = id; }

    public String  getTitle()       { return title; }

    public String  getDescription() { return description; }   // ← новый геттер

    public Status  getStatus()      { return status; }
    public void    setStatus(Status status) { this.status = status; }

    /* equals / hashCode / toString ------------------------------- */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return id == task.id;
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
