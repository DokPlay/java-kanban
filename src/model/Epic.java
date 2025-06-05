package model;

import java.util.ArrayList;

import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    @Override
    public void setStatus(Status status) {
        // Переопределённый метод ничего не делает
        // Статус эпика изменяется только из TaskManager
    }

    public void forceSetStatus(Status status) {
        this.status = status;
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds); // возвращаем копию списка
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    @Override
    public String toString() {
        return "Эпик: " + getName() +
                "\nОписание: " + getDescription() +
                "\nСтатус: " + getStatus() +
                "\nID: " + getId() +
                "\nID подзадач: " + subtaskIds;
    }
}