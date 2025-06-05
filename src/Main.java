import manager.TaskManager;
import model.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // 1. Создаём объекты (2 задачи)
        Task t1 = new Task("Купить ноутбук", "Выбрать модель и оплатить");
        Task t2 = new Task("Помыть машину", "Съездить на мойку и помыть кузов");
        manager.createTask(t1);
        manager.createTask(t2);

        // 2. Эпик 1 с тремя подзадачами
        Epic e1 = new Epic("Переезд", "Переезд в новую квартиру");
        manager.createEpic(e1);
        Subtask st11 = new Subtask("Собрать коробки", "Сложить вещи в коробки", e1.getId());
        Subtask st12 = new Subtask("Упаковать кошку", "Подготовить переноску", e1.getId());
        Subtask st13 = new Subtask("Сказать слова прощания", "Прощальное фото со старой квартирой", e1.getId());
        manager.createSubtask(st11);
        manager.createSubtask(st12);
        manager.createSubtask(st13);

        // 3. Эпик 2 с одной подзадачей
        Epic e2 = new Epic("Важный эпик 2", "Второй эпик для примера");
        manager.createEpic(e2);
        Subtask st21 = new Subtask("Задача 1", "Сделать первый шаг", e2.getId());
        manager.createSubtask(st21);

        // --- Печать списков после создания
        System.out.println("== Все задачи ==");
        print(manager.getAllTasks());
        System.out.println("\n== Все эпики ==");
        print(manager.getAllEpics());
        System.out.println("\n== Все подзадачи ==");
        print(manager.getAllSubtasks());

        // 4. Обновляем статусы
        t1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(t1);
        st11.setStatus(Status.DONE);
        st12.setStatus(Status.IN_PROGRESS);
        st13.setStatus(Status.NEW);
        manager.updateSubtask(st11);
        manager.updateSubtask(st12);
        manager.updateSubtask(st13);
        st21.setStatus(Status.DONE);
        manager.updateSubtask(st21);

        System.out.println("\n== После обновления статусов ==");
        print(manager.getAllTasks());
        System.out.println();
        print(manager.getAllEpics());
        System.out.println();
        print(manager.getAllSubtasks());

        // 5. Удаляем задачу и эпик
        manager.deleteTaskById(t2.getId());
        manager.deleteEpicById(e1.getId());

        System.out.println("\n== После удаления задачи и эпика ==");
        print(manager.getAllTasks());
        System.out.println();
        print(manager.getAllEpics());
        System.out.println();
        print(manager.getAllSubtasks());
    }

    private static void print(List<?> list) {
        if (list.isEmpty()) {
            System.out.println("(пусто)");
            return;
        }
        for (Object o : list) {
            System.out.println(o);
        }
    }
}
