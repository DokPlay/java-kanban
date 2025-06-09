package test.java.manager;

import manager.Managers;
import manager.TaskManager;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Юнит-тесты для {InMemoryTaskManager}.
// Проверяются добавление задач, история просмотров и корректная обработка ошибок.

class InMemoryTaskManagerTest {
    private TaskManager manager;
//Создаёт новый экземпляр менеджера перед каждым тестом.
    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
    }
//Проверяет, что добавленная задача возвращается корректно по ID.
    @Test
    void shouldAddAndReturnTask() {
        Task task = new Task("Test task", "Desc", Status.NEW);
        int id = manager.addNewTask(task);
        Task returned = manager.getTask(id);

        assertNotNull(returned);
        assertEquals(task.getTitle(), returned.getTitle());
    }
//Проверяет, что история просмотров сохраняет порядок и допускает повторы.
    @Test
    void shouldStoreHistoryCorrectly() {
        int id1 = manager.addNewTask(new Task("T1", "", Status.NEW));
        int id2 = manager.addNewTask(new Task("T2", "", Status.NEW));

        manager.getTask(id1);
        manager.getTask(id2);
        manager.getTask(id1);

        List<Task> history = manager.getHistory();
        assertEquals(3, history.size());
        assertEquals("T1", history.get(2).getTitle());
    }
//Проверяет, что при попытке привязать подзадачу к несуществующему эпику будет выброшено исключение.
    @Test
    void shouldThrowIfSubtaskReferencesMissingEpic() {
        Subtask subtask = new Subtask("Ошибка", "Нет эпика", 999); // несуществующий epicId
        assertThrows(IllegalArgumentException.class, () -> manager.addNewSubtask(subtask));
    }

//Проверяет, что история просмотров не превышает 10 элементов.
    @Test
    void historyShouldNotExceedTenEntries() {
        for (int i = 0; i < 12; i++) {
            int id = manager.addNewTask(new Task("T" + i, "", Status.NEW));
            manager.getTask(id);
        }

        List<Task> history = manager.getHistory();
        assertEquals(10, history.size(), "История не должна превышать 10 элементов");
    }
}
