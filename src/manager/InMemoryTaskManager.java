package manager;

import exceptions.NotFoundException;
import exceptions.TaskValidationException; // NEW (sprint-8)
import java.time.LocalDateTime; // NEW (sprint-8)
import java.util.*;
import java.util.stream.Collectors;
import model.*;

/**
 * InMemoryTaskManager хранит задачи в памяти и ведет историю.
 *
 * <p>CHANGED (sprint-8): - приоритизация через TreeSet (startTime); - проверка пересечений при
 * add/update Task/Subtask; - эпики получают расчётные duration/start/end от подзадач; - добавлены
 * protected put*-методы и setNextIdAfterRestore для FileBacked; - часть циклов переписана на stream
 * API.
 *
 * <p>CHANGED (sprint-9): - устранены дубли в prioritized: удаление по id перед переиндексацией.
 */
public class InMemoryTaskManager implements TaskManager {

  /* хранилища */
  protected final Map<Integer, Task> tasks = new HashMap<>();
  protected final Map<Integer, Epic> epics = new HashMap<>();
  protected final Map<Integer, Subtask> subtasks = new HashMap<>();

  /* история */
  private final HistoryManager historyManager = new InMemoryHistoryManager();

  /* ID */
  protected int nextId = 1;

  private int generateId() {
    return nextId++;
  }

  /* приоритизация (sprint-8) */
  // Задачи без startTime сюда не добавляем (по ТЗ).
  private final Comparator<Task> PRIORITY_CMP =
      Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
          .thenComparingInt(Task::getId);

  private final NavigableSet<Task> prioritized = new TreeSet<>(PRIORITY_CMP);

  // sprint-9: удаляем из приоритета все версии по id (независимо от старого startTime)
  private void prioritizedRemoveById(int id) { // sprint-9
    prioritized.removeIf(t -> t.getId() == id);
  }

  // sprint-9: переиндексация элемента в приоритете
  private void prioritizedReindex(Task task) { // sprint-9
    if (task == null) {
      return;
    }
    prioritizedRemoveById(task.getId());
    if (task.getStartTime() != null) {
      prioritized.add(task);
    }
  }

  /* создание */

  @Override
  public int addNewTask(Task task) {
    validateNoOverlaps(task, null);
    int id = generateId();
    task.setId(id);
    tasks.put(id, task);
    prioritizedReindex(task); // sprint-9
    return id;
  }

  @Override
  public int addNewEpic(Epic epic) {
    int id = generateId();
    epic.setId(id);
    epics.put(id, epic);
    return id;
  }

  @Override
  public int addNewSubtask(Subtask subtask) {
    Epic epic = epics.get(subtask.getEpicId());
    if (epic == null) {
      throw new NotFoundException("epic " + subtask.getEpicId() + " not found");
    }
    validateNoOverlaps(subtask, null);
    int id = generateId();
    subtask.setId(id);
    subtasks.put(id, subtask);
    epic.addSubtaskId(id);
    prioritizedReindex(subtask); // sprint-9
    recalcEpic(epic.getId());
    return id;
  }

  /* обновление */

  @Override
  public void updateTask(Task task) {
    if (!tasks.containsKey(task.getId())) {
      throw new NotFoundException("task " + task.getId() + " not found");
    }
    validateNoOverlaps(task, task.getId());
    tasks.put(task.getId(), task);
    prioritizedReindex(task); // sprint-9
  }

  @Override
  public void updateEpic(Epic epic) {
    Epic exist = epics.get(epic.getId());
    if (exist == null) {
      throw new NotFoundException("epic " + epic.getId() + " not found");
    }
    // статус/время эпика пересчитывается от subtask — но позволим обновить title/description
    exist.setTitle(epic.getTitle());
    exist.setDescription(epic.getDescription());
    recalcEpic(exist.getId());
  }

  @Override
  public void updateSubtask(Subtask subtask) {
    if (!subtasks.containsKey(subtask.getId())) {
      throw new NotFoundException("subtask " + subtask.getId() + " not found");
    }
    validateNoOverlaps(subtask, subtask.getId());
    subtasks.put(subtask.getId(), subtask);
    prioritizedReindex(subtask); // sprint-9
    recalcEpic(subtask.getEpicId());
  }

  /* удаление */

  @Override
  public void removeTask(int id) {
    if (!tasks.containsKey(id)) {
      throw new NotFoundException("task " + id + " not found");
    }
    tasks.remove(id);
    prioritizedRemoveById(id); // sprint-9
    historyManager.remove(id);
  }

  @Override
  public void removeEpic(int id) {
    Epic epic = epics.remove(id);
    if (epic == null) {
      throw new NotFoundException("epic " + id + " not found");
    }
    for (int sid : epic.getSubtaskIds()) {
      subtasks.remove(sid);
      prioritizedRemoveById(sid); // sprint-9
      historyManager.remove(sid);
    }
    historyManager.remove(id);
  }

  @Override
  public void removeSubtask(int id) {
    Subtask s = subtasks.remove(id);
    if (s == null) {
      throw new NotFoundException("subtask " + id + " not found");
    }
    Epic epic = epics.get(s.getEpicId());
    if (epic != null) {
      epic.removeSubtaskId(id);
      recalcEpic(epic.getId());
    }
    prioritizedRemoveById(id); // sprint-9
    historyManager.remove(id);
  }

  /* получение + история */

  @Override
  public Task getTask(int id) {
    Task t = tasks.get(id);
    if (t == null) {
      throw new NotFoundException("task " + id + " not found");
    }
    historyManager.add(t);
    return t;
  }

  @Override
  public Epic getEpic(int id) {
    Epic e = epics.get(id);
    if (e == null) {
      throw new NotFoundException("epic " + id + " not found");
    }
    historyManager.add(e);
    return e;
  }

  @Override
  public Subtask getSubtask(int id) {
    Subtask s = subtasks.get(id);
    if (s == null) {
      throw new NotFoundException("subtask " + id + " not found");
    }
    historyManager.add(s);
    return s;
  }

  /* списки */

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
    return epics.containsKey(epicId)
        ? epics.get(epicId).getSubtaskIds().stream()
            .map(subtasks::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        : List.of();
  }

  @Override
  public List<Task> getHistory() {
    return historyManager.getHistory();
  }

  /* prioritized */

  @Override
  public List<Task> getPrioritizedTasks() {
    return new ArrayList<>(prioritized);
  }

  /* расчёт эпика (status/duration/start/end) */

  private void recalcEpic(int epicId) {
    Epic epic = epics.get(epicId);
    if (epic == null) {
      return;
    }
    List<Subtask> subs =
        epic.getSubtaskIds().stream().map(subtasks::get).filter(Objects::nonNull).toList();
    epic.recalcFromSubtasks(subs);
  }

  /* пересечения (sprint-8) */

  private void validateNoOverlaps(Task candidate, Integer selfId) {
    if (candidate.getStartTime() == null || candidate.getDuration() == null) {
      return;
    }
    boolean intersect =
        prioritized.stream()
            .filter(t -> selfId == null || t.getId() != selfId)
            .anyMatch(t -> isOverlap(candidate, t));
    if (intersect) {
      throw new TaskValidationException("Задача пересекается по времени с другой");
    }
  }

  // Пересечение отрезков: [A.start, A.end) и [B.start, B.end)
  private static boolean isOverlap(Task a, Task b) {
    LocalDateTime as = a.getStartTime();
    LocalDateTime bs = b.getStartTime();
    if (as == null || bs == null) {
      return false;
    }
    var ad = a.getDuration();
    var bd = b.getDuration();
    if (ad == null || bd == null) {
      return false;
    }
    LocalDateTime ae = a.getEndTime();
    LocalDateTime be = b.getEndTime();
    return as.isBefore(be) && bs.isBefore(ae);
  }

  /* поддержка FileBacked (preserve id / nextId) */

  protected void putTaskPreserveId(Task t) {
    tasks.put(t.getId(), t);
    prioritizedReindex(t); // sprint-9
  }

  protected void putEpicPreserveId(Epic e) {
    epics.put(e.getId(), e);
  }

  protected void putSubtaskPreserveId(Subtask s) {
    subtasks.put(s.getId(), s);
    Epic epic = epics.get(s.getEpicId());
    if (epic != null) {
      epic.addSubtaskId(s.getId());
    }
    prioritizedReindex(s); // sprint-9
  }

  protected void setNextIdAfterRestore(int next) {
    this.nextId = Math.max(this.nextId, next);
    epics.keySet().forEach(this::recalcEpic);
  }
}
