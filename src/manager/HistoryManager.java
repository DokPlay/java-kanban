package manager;

import java.util.List;
import model.Task;

/** Интерфейс менеджера истории просмотров задач. */
public interface HistoryManager {

  void add(Task task);

  void remove(int id);

  List<Task> getHistory();
}
