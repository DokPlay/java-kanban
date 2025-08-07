package manager;

import model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);    // записать просмотр
    void remove(int id);    // удалить по id (нужно при удалении задач)
    List<Task> getHistory();// вернуть историю в порядке просмотра
}
