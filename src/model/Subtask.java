package model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description, Status.NEW);
        if (epicId <= 0) {
            throw new IllegalArgumentException("Неверный epicId");
        }
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toCsvRow() {
        return String.join(",",
                String.valueOf(id),
                getType().name(),
                title,
                status.name(),
                description,
                String.valueOf(epicId)
        );
    }
}
