package manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration; // NEW (sprint-8)
import java.time.LocalDateTime; // NEW (sprint-8)
import java.time.format.DateTimeFormatter; // NEW (sprint-8)
import java.util.ArrayList;
import java.util.List;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import model.TaskType;
import util.CsvUtils; // TODO(review): парсинг времени вынесен в утилиту

/**
 * Менеджер с сохранением состояния в файл (CSV). CHANGED (sprint-8): - расширен CSV-формат:
 * добавлены колонки durationMinutes и startTime; - добавлена обратная совместимость чтения старого
 * формата; - на restore используем put*PreserveId + setNextIdAfterRestore.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {

  private final File file;

  // Единый формат для CSV
  private static final DateTimeFormatter CSV_TIME_FMT = Task.CSV_TIME_FMT;

  /* ───────────── фабрика ───────────── */

  public FileBackedTaskManager(File file) {
    this.file = file;
  }

  @SuppressWarnings("unused")
  public static FileBackedTaskManager loadFromFile(File file) {
    FileBackedTaskManager manager = new FileBackedTaskManager(file);
    manager.restore();
    return manager;
  }

  /* ───────────── сохранение ───────────── */

  /** Сохраняет все задачи в CSV: id,type,name,status,description,durationMinutes,startTime,epic */
  private void save() {
    try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
      writer.write("id,type,name,status,description,durationMinutes,startTime,epic");
      writer.newLine();

      // Порядок не критичен, но читается приятнее
      for (Task task : getTasks()) {
        writer.write(task.toCsvRow());
        writer.newLine();
      }
      for (Epic epic : getEpics()) {
        writer.write(epic.toCsvRow());
        writer.newLine();
      }
      for (Subtask subtask : getSubtasks()) {
        writer.write(subtask.toCsvRow());
        writer.newLine();
      }

    } catch (IOException ex) {
      throw new ManagerSaveException("Не удалось сохранить файл", ex);
    }
  }

  /* ───────────── восстановление ───────────── */

  /** Читает CSV и восстанавливает состояние. */
  private void restore() {
    if (!file.exists()) {
      return;
    }

    List<Epic> epics = new ArrayList<>();
    List<Task> tasks = new ArrayList<>();
    List<Subtask> subtasks = new ArrayList<>();

    int maxId = 0; // TODO(review): считаем maxId за один проход при чтении файла

    try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
      String header = reader.readLine(); // заголовок
      if (header == null) {
        return;
      }

      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }

        Task task = fromCsv(line);
        maxId = Math.max(maxId, task.getId()); // TODO(review): обновляем maxId на лету

        if (task instanceof Epic epic) {
          epics.add(epic);
        } else if (task instanceof Subtask subtask) {
          subtasks.add(subtask);
        } else if (task.getType() == TaskType.TASK) { // базовая Task
          tasks.add(task);
        } else {
          throw new IllegalStateException(
              "Неизвестный подкласс задачи (id="
                  + task.getId()
                  + "): "
                  + task.getClass().getName());
        }
      }
    } catch (IOException ex) {
      throw new ManagerSaveException("Не удалось прочитать файл", ex);
    }

    // Важно: сначала эпики, затем задачи, затем подзадачи
    for (Epic epic : epics) {
      super.putEpicPreserveId(epic);
    }
    for (Task task : tasks) {
      super.putTaskPreserveId(task);
    }
    for (Subtask subtask : subtasks) {
      super.putSubtaskPreserveId(subtask);
    }

    // TODO(review): без дополнительных циклов — используем maxId, посчитанный при чтении
    super.setNextIdAfterRestore(maxId + 1);
  }

  /* ───────────── CSV утилиты ───────────── */

  private static Task fromCsv(String csv) {
    String[] taskParts = csv.split(",", -1); // TODO(review): не используем односимвольные имена
    // Старый формат Sprint 7: id,type,name,status,description,epic  (6 полей, epic только у
    // subtask)
    // Новый формат Sprint 8: id,type,name,status,description,durationMinutes,startTime,epic  (8
    // полей)
    if (taskParts.length != 6 && taskParts.length != 8) {
      throw new ManagerSaveException("Некорректная строка CSV: " + csv);
    }

    int id = Integer.parseInt(taskParts[0]);
    TaskType type = TaskType.valueOf(taskParts[1]);
    String name = taskParts[2];
    Status status = Status.valueOf(taskParts[3]);
    String description = taskParts[4];

    String durStr = taskParts.length == 8 ? taskParts[5] : "";
    String startStr = taskParts.length == 8 ? taskParts[6] : "";
    // после проверки выше длина может быть только 6 или 8
    String epicStr = taskParts.length == 8 ? taskParts[7] : taskParts[5];

    Duration duration = durStr.isBlank() ? null : Duration.ofMinutes(Long.parseLong(durStr));
    LocalDateTime startTime =
        CsvUtils.parseTimeOrNull(
            startStr, CSV_TIME_FMT); // TODO(review): парсинг времени из утилиты

    switch (type) {
      case TASK:
        {
          Task task = new Task(name, description, status);
          task.setId(id);
          task.setDuration(duration);
          task.setStartTime(startTime);
          return task;
        }
      case EPIC:
        {
          Epic epic = new Epic(name, description);
          epic.setId(id);
          epic.setStatus(status);
          // duration/start/end будут пересчитаны после загрузки subtask
          return epic;
        }
      case SUBTASK:
        {
          int epicId = (epicStr == null || epicStr.isBlank()) ? 0 : Integer.parseInt(epicStr);
          Subtask subtask = new Subtask(name, description, status, epicId);
          subtask.setId(id);
          subtask.setDuration(duration);
          subtask.setStartTime(startTime);
          return subtask;
        }
      default:
        throw new IllegalStateException("Неизвестный тип: " + type);
    }
  }

  /* ───────────── переопределения с автосохранением ───────────── */

  @Override
  public int addNewTask(Task task) {
    int id = super.addNewTask(task);
    save();
    return id;
  }

  @Override
  public int addNewEpic(Epic epic) {
    int id = super.addNewEpic(epic);
    save();
    return id;
  }

  @Override
  public int addNewSubtask(Subtask subtask) {
    int id = super.addNewSubtask(subtask);
    save();
    return id;
  }

  @Override
  public void updateTask(Task task) {
    super.updateTask(task);
    save();
  }

  @Override
  public void updateEpic(Epic epic) {
    super.updateEpic(epic);
    save();
  }

  @Override
  public void updateSubtask(Subtask subtask) {
    super.updateSubtask(subtask);
    save();
  }

  @Override
  public void removeTask(int id) {
    super.removeTask(id);
    save();
  }

  @Override
  public void removeEpic(int id) {
    super.removeEpic(id);
    save();
  }

  @Override
  public void removeSubtask(int id) {
    super.removeSubtask(id);
    save();
  }
}
