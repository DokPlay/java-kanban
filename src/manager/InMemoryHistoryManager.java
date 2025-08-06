package manager;

import model.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    /* ───── узел двусвязного списка ───── */
    private static class Node {
        Task data;
        Node prev;
        Node next;
        Node(Node prev, Task data, Node next) {
            this.prev = prev;
            this.data = data;
            this.next = next;
        }
    }

    /* ───── поля ───── */
    private final Map<Integer, Node> index = new HashMap<>();
    private Node head;
    private Node tail;

    /* ───── вспомогательные ───── */
    private void linkLast(Task task) {
        Node oldTail = tail;
        Node n = new Node(oldTail, task, null);
        tail = n;
        if (oldTail == null) head = n; else oldTail.next = n;
    }
    private void removeNode(Node n) {
        if (n == null) return;
        Node p = n.prev, nx = n.next;
        if (p != null) p.next = nx; else head = nx;
        if (nx != null) nx.prev = p; else tail = p;
    }
    private List<Task> getTasks() {
        List<Task> list = new ArrayList<>();
        for (Node n = head; n != null; n = n.next) list.add(n.data);
        return list;
    }

    /* ───── интерфейс ───── */
    @Override
    public void add(Task task) {
        if (task == null) return;
        Node old = index.remove(task.getId());
        removeNode(old);            // убрал предыдущее вхождение
        linkLast(task);             // добавил новое в конец
        index.put(task.getId(), tail);
    }
    @Override
    public void remove(int id) {
        Node n = index.remove(id);
        removeNode(n);
    }
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
