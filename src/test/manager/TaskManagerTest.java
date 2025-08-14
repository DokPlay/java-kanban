package manager;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void epicStatusRules() {
        int epicId = manager.addNewEpic(new Epic("E", "d"));

        // a) все NEW
        int s1 = manager.addNewSubtask(new Subtask("s1", "", epicId));
        int s2 = manager.addNewSubtask(new Subtask("s2", "", epicId));
        assertEquals(Status.NEW, manager.getEpic(epicId).getStatus());

        // b) все DONE
        Subtask us1 = manager.getSubtask(s1);
        us1.setStatus(Status.DONE);
        manager.updateSubtask(us1);

        Subtask us2 = manager.getSubtask(s2);
        us2.setStatus(Status.DONE);
        manager.updateSubtask(us2);

        assertEquals(Status.DONE, manager.getEpic(epicId).getStatus());

        // c) NEW + DONE => IN_PROGRESS
        us1.setStatus(Status.NEW);
        manager.updateSubtask(us1);
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus());

        // d) есть IN_PROGRESS => IN_PROGRESS
        us2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(us2);
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus());
    }

    @Test
    void prioritizedAndNoIntersections() {
        // две задачи подряд без пересечения
        Task t1 = new Task("t1", "");
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(30));

        Task t2 = new Task("t2", "");
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30));
        t2.setDuration(Duration.ofMinutes(30));

        manager.addNewTask(t1);
        manager.addNewTask(t2);

        List<Task> pr = manager.getPrioritizedTasks();
        assertEquals(2, pr.size());
        assertEquals("t1", pr.get(0).getTitle());
        assertEquals("t2", pr.get(1).getTitle());

        // пересечение должно падать
        Task t3 = new Task("t3", "");
        t3.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 15));
        t3.setDuration(Duration.ofMinutes(30));
        assertThrows(RuntimeException.class, () -> manager.addNewTask(t3));
    }

    @Test
    void epicTimeIsCalculatedFromSubtasks() {
        int epicId = manager.addNewEpic(new Epic("E", ""));

        Subtask s1 = new Subtask("a", "", epicId);
        s1.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 0));
        s1.setDuration(Duration.ofMinutes(30));
        int s1id = manager.addNewSubtask(s1);

        Subtask s2 = new Subtask("b", "", epicId);
        s2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        s2.setDuration(Duration.ofMinutes(90));
        int s2id = manager.addNewSubtask(s2);
        assertTrue(s2id > 0); // используем переменную, чтобы не было предупреждения

        Epic e = manager.getEpic(epicId);
        assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), e.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 1, 11, 30), e.getEndTime());
        assertEquals(120, e.getDuration().toMinutes()); // 30 + 90

        // удалил одну — пересчёт
        manager.removeSubtask(s1id);
        e = manager.getEpic(epicId);
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), e.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 1, 11, 30), e.getEndTime());
        assertEquals(90, e.getDuration().toMinutes());
    }
}
