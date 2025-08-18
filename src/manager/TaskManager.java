package manager;

import java.util.List;
import model.Epic;
import model.Subtask;
import model.Task;

/** Интерфейс менеджера задач (Sprint 7 + новые методы Sprint 8). */
public interface TaskManager {

  // Создание
  int addNewTask(Task task);

  int addNewEpic(Epic epic);

  int addNewSubtask(Subtask subtask);

  // Обновление
  void updateTask(Task task);

  void updateEpic(Epic epic);

  void updateSubtask(Subtask subtask);

  // Удаление
  void removeTask(int id);

  void removeEpic(int id);

  void removeSubtask(int id);

  // Получение (одна)
  Task getTask(int id);

  Epic getEpic(int id);

  Subtask getSubtask(int id);

  // Получение (списки)
  List<Task> getTasks();

  List<Epic> getEpics();

  List<Subtask> getSubtasks();

  List<Subtask> getEpicSubtasks(int epicId);

  // История
  List<Task> getHistory();

  // Prioritized (sprint-8)
  /** Задачи и подзадачи по приоритету startTime; эпики не включаются. */
  List<Task> getPrioritizedTasks();
}
