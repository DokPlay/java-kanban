package model;

import java.util.ArrayList;
import java.util.List;

/** Эпик — задача, содержащая подзадачи. */
public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    /** Возвращает копию списка идентификаторов подзадач. */
    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    /** Добавляет id подзадачи (эпик не может ссылаться сам на себя). */
    public void addSubtaskId(int id) {
        if (id == this.id) {
            throw new IllegalArgumentException("Эпик не может быть собственным Subtask");
        }
        subtaskIds.add(id);
    }
}
