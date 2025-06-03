public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() { return epicId; }
    public void setEpicId(int epicId) { this.epicId = epicId; }

    @Override
    public String toString() {
        return "Подзадача: " + getName() +
                "\nОписание: " + getDescription() +
                "\nСтатус: " + getStatus() +
                "\nID: " + getId() +
                "\nЭпик ID: " + epicId;
    }
}