import manager.Managers;
import manager.TaskManager;
import model.*;

// Демонстрация базовой работы с менеджером задач
public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // === Добавление задач ===
        int id1    = manager.addNewTask(new Task("Задача 1", "Описание задачи", Status.NEW));
        int epicId = manager.addNewEpic(new Epic("Эпик 1", "Описание эпика"));
        int subId  = manager.addNewSubtask(new Subtask("Подзадача 1", "Описание подзадачи", epicId));
        manager.addNewSubtask(new Subtask("Подзадача 2", "Ещё одна", epicId));

        // === Используем возвращаемые списки (убираем жёлтые лампы) TODO: это просто для себя подчеркиваю ===
        int tasksCount     = manager.getTasks().size();
        int epicsCount     = manager.getEpics().size();
        int subtasksCount  = manager.getSubtasks().size();
        int epicSubCount   = manager.getEpicSubtasks(epicId).size();
        System.out.printf(
                "Всего: tasks=%d, epics=%d, subtasks=%d; у эпика %d подзадач=%d%n",
                tasksCount, epicsCount, subtasksCount, epicId, epicSubCount
        );

        // === Получение задач (для истории просмотров) — без пустых if ===
        boolean viewedTask1 = manager.getTask(id1) != null;
        boolean viewedEpic  = manager.getEpic(epicId) != null;
        boolean viewedSub   = manager.getSubtask(subId) != null;
        boolean viewedTask2 = manager.getTask(id1) != null;

        // просто используем значения, чтобы инспекция была довольна
        System.out.printf("Просмотры: t1=%b, epic=%b, sub=%b, t1-again=%b%n",
                viewedTask1, viewedEpic, viewedSub, viewedTask2);


        // === Вывод истории просмотров ===
        System.out.println("=== История просмотров ===");
        for (Task task : manager.getHistory()) {
            System.out.printf(
                    "%s (ID: %d)\nЗаголовок: %s\nСтатус: %s\n---\n",
                    getTypeName(task),
                    task.getId(),
                    task.getTitle(),
                    getStatusName(task.getStatus())
            );
        }
    }

    private static String getTypeName(Task task) {
        if (task instanceof Epic) return "Эпик";
        if (task instanceof Subtask) return "Подзадача";
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
