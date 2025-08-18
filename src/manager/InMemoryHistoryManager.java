package manager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import model.Task;

public class InMemoryHistoryManager implements HistoryManager {

  private static final int MAX = 10;
  private final LinkedHashMap<Integer, Task> order = new LinkedHashMap<>();

  @Override
  public void add(Task task) {
    if (task == null) {
      return;
    }
    int id = task.getId();
    // Дедупликация.
    order.remove(id);
    order.put(id, task);
    // Ограничиваем размер.
    while (order.size() > MAX) {
      Integer firstKey = order.keySet().iterator().next();
      order.remove(firstKey);
    }
  }

  @Override
  public void remove(int id) {
    order.remove(id);
  }

  @Override
  public List<Task> getHistory() {
    return new ArrayList<>(order.values());
  }
}
