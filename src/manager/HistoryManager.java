package manager;
//Интерфейс менеджера истории просмотров задач.
import model.Task;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
}