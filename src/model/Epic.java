package model;

import java.util.ArrayList;
import java.util.List;

/** Эпик — агрегирующая задача, содержит список id подзадач. */
public class Epic extends Task {

    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description, Status.NEW);
    }

    /*--------------- новый метод ---------------*/
    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }
    /*-------------------------------------------*/

    /** Возвращает копию списка id подзадач (инкапсуляция). */
    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    /** Добавляет id подзадачи к эпику. */
    public void addSubtaskId(int id) {
        if (id == this.id) {
            throw new IllegalArgumentException("Эпик не может быть собственным сабтаском");
        }
        subtaskIds.add(id);
    }
}
