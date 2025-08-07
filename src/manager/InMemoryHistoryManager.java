package manager;

import model.Task;
import java.util.*;

/** HistoryManager на базе двусвязного списка + HashMap<id, node> */
public class InMemoryHistoryManager implements HistoryManager {

    /* ───── узел списка ───── */
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

    /** добавляем просмотр в хвост */
    private void linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);   //  n → newNode
        tail = newNode;

        if (oldTail == null) {
            head = newNode;                             //  фигурные скобки
        } else {
            oldTail.next = newNode;                     //  фигурные скобки
        }
    }

    /** удаляем произвольный узел */
    private void removeNode(Node target) {
        if (target == null) {
            return;
        }

        Node prev = target.prev;
        Node next = target.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;                                //  фигурные скобки
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;                                //  фигурные скобки
        }
    }

    /** выгружаем историю списком */
    private List<Task> getTasks() {
        List<Task> list = new ArrayList<>();
        for (Node current = head; current != null; current = current.next) {
            list.add(current.data);
        }
        return list;
    }

    /* ───── HistoryManager API ───── */

    @Override
    public void add(Task task) {
        if (task == null) {
            return;                                     //  фигурные скобки
        }

        /* если id уже есть — убираем старый узел */
        Node duplicate = index.remove(task.getId());
        removeNode(duplicate);

        /* вносим новый просмотр */
        linkLast(task);
        index.put(task.getId(), tail);
    }

    @Override
    public void remove(int id) {
        Node node = index.remove(id);
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
