package manager;

import manager.HistoryManager;
import manager.Managers;
import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  Юнит-тесты самого HistoryManager
 */
class HistoryManagerTest {

    private HistoryManager hm;

    private Task t1;
    private Task t2;
    private Task t3;

    @BeforeEach
    void setUp() {
        hm = Managers.getDefaultHistory();     // или new InMemoryHistoryManager()

        //   id задаём вручную, чтобы HistoryManager.remove(id) работал корректно
        t1 = new Task("T-1", "descr1", Status.NEW); t1.setId(1);
        t2 = new Task("T-2", "descr2", Status.NEW); t2.setId(2);
        t3 = new Task("T-3", "descr3", Status.NEW); t3.setId(3);
    }

    /** add(): без дубликатов, последний просмотр переносится в конец */
    @Test
    void add_movesTaskToTail_withoutDuplicates() {
        hm.add(t1);
        hm.add(t2);
        hm.add(t3);
        hm.add(t2);                   // повторный просмотр t2

        List<Task> history = hm.getHistory();
        assertEquals(List.of(t1, t3, t2), history,
                "Повторный просмотр должен перемещать задачу в конец истории без дублирования");
    }

    /** remove(): удаляет узел из середины за O(1) */
    @Test
    void remove_deletesNodeFromAnyPosition() {
        hm.add(t1);
        hm.add(t2);
        hm.add(t3);

        hm.remove(2);                 // удаляем t2 (из середины списка)

        List<Task> history = hm.getHistory();
        assertEquals(List.of(t1, t3), history,
                "После удаления задачи из середины истории в списке должны остаться t1 и t3");
    }
}
