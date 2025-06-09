package manager;

import model.Task;
import java.util.*;
//Хранит максимум 10 последних задач. При превышении лимита
 // самая старая задача удаляется.
public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY = 10;
    private final Deque<Task> history = new ArrayDeque<>();

    @Override
    public void add(Task task) {
        history.addLast(task);
        if (history.size() > MAX_HISTORY) {
            history.pollFirst();
        }
    }
//Возвращает список просмотренных задач (в порядке просмотра).
    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}