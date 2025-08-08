package manager;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit-тесты InMemoryTaskManager + HistoryManager.
 */
class InMemoryTaskManagerTest {

    private TaskManager tm;

    @BeforeEach
    void setUp() {
        tm = Managers.getDefault();   // InMemoryTaskManager
    }

    /* -------- 1. Дубликаты не сохраняются -------- */
    @Test
    void addDuplicates_keepsOnlyLastView() {
        int id = tm.addNewTask(new Task("T", "d", Status.NEW));

        tm.getTask(id);
        tm.getTask(id);
        tm.getTask(id);

        List<Task> history = tm.getHistory();
        assertEquals(1, history.size(),
                "В истории должен остаться единственный просмотр");
        assertEquals(id, history.get(0).getId());//TODO: если именно нужно от Java 21 и более то нужно заменить на
        // assertEquals(id, history.getFirst().getId());
    }

    /* -------- 2. История может быть > 10 -------- */
    @Test
    void historyCanGrowMoreThanTen() {
        for (int i = 0; i < 20; i++) {
            int id = tm.addNewTask(new Task("task-" + i, "", Status.NEW));
            tm.getTask(id);
        }
        assertEquals(20, tm.getHistory().size(),
                "История должна содержать все 20 просмотров");
    }

    /* -------- 3. Удаление чистит историю -------- */
    @Test
    void deletingTask_removesItFromHistory() {
        int id = tm.addNewTask(new Task("X", "", Status.NEW));
        tm.getTask(id);

        tm.removeTask(id);

        assertTrue(tm.getHistory().isEmpty(),
                "После удаления задачи записи о ней в истории быть не должно");
    }
}
