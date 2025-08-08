package manager;

import model.*;

import java.util.*;

/**
 * InMemoryTaskManager хранит задачи в памяти и ведет историю.
 */
public class InMemoryTaskManager implements TaskManager {

    /* ---------- хранилища ---------- */
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    /* ---------- история ---------- */
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    /* ---------- генератор ID ---------- */
    protected int nextId = 1;

    private int generateId() {
        return nextId++;
    }

    /* ---------- создание ---------- */
    @Override
    public int addNewTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public int addNewEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не найден");
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        return subtask.getId();
    }

    /* ---------- обновление ---------- */
    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }

    @Override
    public void updateSubtask(Subtask s) {
        if (subtasks.containsKey(s.getId())) {
            subtasks.put(s.getId(), s);
        }
    }

    /* ---------- удаление ---------- */
    @Override
    public void removeTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int sid : epic.getSubtaskIds()) {
                subtasks.remove(sid);
                historyManager.remove(sid);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void removeSubtask(int id) {
        Subtask s = subtasks.remove(id);
        if (s != null) {
            Epic epic = epics.get(s.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
            }
        }
        historyManager.remove(id);
    }

    /* ---------- получение + история ---------- */
    @Override
    public Task getTask(int id) {
        Task t = tasks.get(id);
        if (t != null) {
            historyManager.add(t);
        }
        return t;
    }

    @Override
    public Epic getEpic(int id) {
        Epic e = epics.get(id);
        if (e != null) {
            historyManager.add(e);
        }
        return e;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask s = subtasks.get(id);
        if (s != null) {
            historyManager.add(s);
        }
        return s;
    }

    /* ---------- списки ---------- */
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int id : epic.getSubtaskIds()) {
                Subtask s = subtasks.get(id);
                if (s != null) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    /* ---------- защищённые хуки для восстановления из файла ---------- */

    /** Кладем задачу с уже заданным id (не трогаем историю, TODO: без увеличения nextId). */
    protected void putTaskPreserveId(Task task) {
        tasks.put(task.getId(), task);
        bumpNextId(task.getId());
    }

    /** Кладем эпик с уже заданным id. */
    protected void putEpicPreserveId(Epic epic) {
        epics.put(epic.getId(), epic);
        bumpNextId(epic.getId());
    }

    /** Кладем Subtask с уже заданным id и привязываем к эпику. */
    protected void putSubtaskPreserveId(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
        }
        bumpNextId(subtask.getId());
    }
    private void bumpNextId(int usedId) {
        if (usedId >= nextId) {
            nextId = usedId + 1;
        }
    }

    /** Вызывается после восстановления, чтобы новые id шли дальше. */
    protected void setNextIdAfterRestore(int nextId) {
        this.nextId = Math.max(this.nextId, nextId);
    }
}
