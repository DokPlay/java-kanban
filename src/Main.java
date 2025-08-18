import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {
  public static void main(String[] args) {
    TaskManager manager = Managers.getDefault();

    // Добавление задач
    int id1 = manager.addNewTask(new Task("Задача 1", "Описание задачи", Status.NEW));
    int epicId = manager.addNewEpic(new Epic("Эпик 1", "Описание эпика"));
    int subId = manager.addNewSubtask(new Subtask("Подзадача 1", "Описание подзадачи", epicId));
    manager.addNewSubtask(new Subtask("Подзадача 2", "Ещё одна", epicId));

    // Краткая сводка
    int tasksCount = manager.getTasks().size();
    int epicsCount = manager.getEpics().size();
    int subtasksCount = manager.getSubtasks().size();
    int epicSubCount = manager.getEpicSubtasks(epicId).size();
    System.out.printf(
      "Всего: tasks=%d, epics=%d, subtasks=%d; у эпика %d подзадач=%d%n",
      tasksCount, epicsCount, subtasksCount, epicId, epicSubCount);

    // Получения для истории просмотров
    manager.getTask(id1);
    manager.getEpic(epicId);
    manager.getSubtask(subId);
    manager.getTask(id1); // повторно

    // Вывод истории просмотров
    System.out.println("=== История просмотров ===");
    for (Task task : manager.getHistory()) {
      System.out.printf(
        "%s (ID: %d)\nЗаголовок: %s\nСтатус: %s\n---\n",
        getTypeName(task), task.getId(), task.getTitle(), getStatusName(task.getStatus()));
    }
  }

  private static String getTypeName(Task task) {
    if (task instanceof Epic) {
      return "Эпик";
    }
    if (task instanceof Subtask) {
      return "Подзадача";
    }
    return "Задача";
  }

  private static String getStatusName(Status status) {
    return switch (status) {
      case NEW -> "Новая";
      case IN_PROGRESS -> "В процессе";
      case DONE -> "Выполнена";
    };
  }
}
