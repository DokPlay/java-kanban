import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public List<Integer> getSubtaskIds() { return subtaskIds; }

    @Override
    public String toString() {
        return "Эпик: " + getName() +
                "\nОписание: " + getDescription() +
                "\nСтатус: " + getStatus() +
                "\nID: " + getId() +
                "\nID подзадач: " + subtaskIds;
    }
}