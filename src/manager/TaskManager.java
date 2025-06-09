package manager;

import model.*;
import java.util.List;
// Интерфейс менеджера задач.
// Определяет базовые методы для управления обычными задачами, эпиками и подзадачами.
// Также предоставляет методы для получения истории просмотров.
public interface TaskManager {

    //Создание задач всех типов
    int addNewTask(Task task);
    int addNewEpic(Epic epic);
    int addNewSubtask(Subtask subtask);

    //Обновление задач всех типов
    void updateTask(Task task);
    void updateEpic(Epic epic);
    void updateSubtask(Subtask subtask);

    //Удаление задач всех типов
    void removeTask(int id);
    void removeEpic(int id);
    void removeSubtask(int id);

    Task getTask(int id);
    Epic getEpic(int id);
    Subtask getSubtask(int id);

    List<Task> getTasks();
    List<Epic> getEpics();
    List<Subtask> getSubtasks();
    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getHistory();
}