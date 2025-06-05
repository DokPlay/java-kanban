package manager;

import model.*;

import java.util.*;

// Менеджер задач. Хранит задачи всех трёх типов и выполняет основные операции, описанные в техническом задании.

public class TaskManager {
    // Отдельные таблицы под разные типы задач — упрощает выборку.
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private int nextId = 1;// генератор идентификаторов
    // === Служебные методы ===
    private int generateId() {
        return nextId++;
    }

    private void updateEpicStatus(Epic epic) {
        List<Integer> ids = epic.getSubtaskIds();
        if (ids.isEmpty()) {
            epic.forceSetStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int id : ids) {
            Status st = subtasks.get(id).getStatus();
            if (st != Status.NEW) allNew = false;
            if (st != Status.DONE) allDone = false;
        }

        if (allDone) {
            epic.forceSetStatus(Status.DONE);
        } else if (allNew) {
            epic.forceSetStatus(Status.NEW);
        } else {
            epic.forceSetStatus(Status.IN_PROGRESS);
        }
    }

    // === Методы для обычных задач ===
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    // === Методы для эпиков ===
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllEpics() {
        subtasks.clear();                            // Все подзадачи можно удалить сразу
        epics.clear();
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic); // статус NEW при пустом списке подзадач
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic storedEpic = epics.get(epic.getId());
            storedEpic.setName(epic.getName());
            storedEpic.setDescription(epic.getDescription());
            updateEpicStatus(storedEpic); // пересчёт статуса
        }
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
        }
    }

    // === Методы для подзадач ===
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllSubtasks() {
        // Чистим списки подзадач у эпиков
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            System.out.println("Ошибка: Эпик с id " + subtask.getEpicId() + " не найден.");
            return;
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            if (!epics.containsKey(subtask.getEpicId())) {
                System.out.println("Ошибка: Эпик с id " + subtask.getEpicId() + " не найден.");
                return;
            }
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(epics.get(subtask.getEpicId()));
        }
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    // === Дополнительный метод ===
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();

        List<Subtask> result = new ArrayList<>();
        for (int id : epic.getSubtaskIds()) {
            result.add(subtasks.get(id));
        }
        return result;
    }
}
