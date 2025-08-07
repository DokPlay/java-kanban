package manager;

import model.*;
import java.util.*;

// InMemoryTaskManager хранит задачи, эпики и подзадачи в памяти
// + ведёт историю просмотров через HistoryManager.
public class InMemoryTaskManager implements TaskManager {

    /* ---------- хранилища ---------- */
    private final Map<Integer, Task> tasks     = new HashMap<>();
    private final Map<Integer, Epic> epics     = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    /* ---------- менеджер истории ---------- */
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int nextId = 1;
    private int generateId() { return nextId++; }

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
        if (epic == null) throw new IllegalArgumentException("Эпик не найден");

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        return id;
    }

    /* ---------- обновление ---------- */
    @Override public void updateTask(Task task)    { if (tasks.containsKey(task.getId()))    tasks.put(task.getId(), task); }
    @Override public void updateEpic(Epic epic)    { if (epics.containsKey(epic.getId()))    epics.put(epic.getId(), epic); }
    @Override public void updateSubtask(Subtask s) { if (subtasks.containsKey(s.getId()))    subtasks.put(s.getId(), s); }

    /* ---------- удаление ---------- */
    @Override
    public void removeTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);          // добавил удаляем из истории
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            // удаляем все подзадачи эпика
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
                historyManager.remove(subId);   // добавил подзадача из истории
            }
            historyManager.remove(id);          // добавил сам эпик из истории
        }
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) epic.getSubtaskIds().remove((Integer) id);
        }
        historyManager.remove(id);              // добавил subtask из истории
    }

    /* ---------- получение + запись в историю ---------- */
    @Override
    public Task     getTask(int id)     { Task t = tasks.get(id);     if (t != null) historyManager.add(t); return t; }
    @Override
    public Epic     getEpic(int id)     { Epic e = epics.get(id);     if (e != null) historyManager.add(e); return e; }
    @Override
    public Subtask  getSubtask(int id)  { Subtask s = subtasks.get(id); if (s != null) historyManager.add(s); return s; }

    /* ---------- списки ---------- */
    @Override public List<Task>     getTasks()        { return new ArrayList<>(tasks.values()); }
    @Override public List<Epic>     getEpics()        { return new ArrayList<>(epics.values()); }
    @Override public List<Subtask>  getSubtasks()     { return new ArrayList<>(subtasks.values()); }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int id : epic.getSubtaskIds()) {
                Subtask s = subtasks.get(id);
                if (s != null) result.add(s);
            }
        }
        return result;
    }

    /* ---------- история ---------- */
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
