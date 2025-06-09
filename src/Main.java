import manager.Managers;
import manager.TaskManager;
import model.*;


// Демонстрация базовой работы с менеджером задач:
//добавление задач, получение и история просмотров.

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // === Добавление задач ===
        int id1 = manager.addNewTask(new Task("Задача 1", "Описание задачи", Status.NEW));
        int id2 = manager.addNewEpic(new Epic("Эпик 1", "Описание эпика"));
        int id3 = manager.addNewSubtask(new Subtask("Подзадача 1", "Описание подзадачи", id2));

        // === Получение задач (для истории просмотров) ===
        manager.getTask(id1);
        manager.getEpic(id2);
        manager.getSubtask(id3);
        manager.getTask(id1); // повторное обращение к задаче

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

    // понятный тип задачи
    private static String getTypeName(Task task) {
        if (task instanceof Epic) return "Эпик";
        if (task instanceof Subtask) return "Подзадача";
        return "Задача";
    }

    // Перевод статуса на русский
    private static String getStatusName(Status status) {
        return switch (status) {
            case NEW -> "Новая";
            case IN_PROGRESS -> "В процессе";
            case DONE -> "Выполнена";
        };
    }
}
