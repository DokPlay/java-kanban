package test.java.manager;

import manager.Managers;
import manager.TaskManager;
import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  Интеграционный тест: TaskManager ↔ HistoryManager
 */
class TaskManagerHistoryIntegrationTest {

    private TaskManager tm;

    @BeforeEach
    void setUp() {
        tm = Managers.getDefault();   // new InMemoryTaskManager()
    }

    /** Удаление задачи очищает историю */
    @Test
    void deletingTask_removesItFromHistory() {
        int id = tm.addNewTask(new Task("Task-1", "descr", Status.NEW));

        tm.getTask(id);                         // помещаем в историю
        assertEquals(1, tm.getHistory().size(),
                "После просмотра история должна содержать одну запись");

        tm.removeTask(id);                      // удаляем задачу
        assertTrue(tm.getHistory().isEmpty(),
                "После удаления задачи история должна быть пустой");
    }
}
